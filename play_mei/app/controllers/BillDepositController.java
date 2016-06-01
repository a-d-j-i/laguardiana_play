package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import java.util.List;
import machines.status.MachineBillDepositStatus;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import models.BillDeposit;
import models.Configuration;
import models.ModelFacade;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import validation.FormCurrency;
import validation.FormDepositUserCodeBillReference;

public class BillDepositController extends Controller {

    static MachineStatus status;

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        status = ModelFacade.getCurrentStatus();
        if (status == null) {
            Logger.error("status is null");
            Application.index();
        }
        if (request.isAjax()) {
            return;
        }
        if (Secure.isLocked(status.getCurrentUserId())) {
            ErrorController.onError();
        }
        String neededAction = status.getNeededAction();
        if (neededAction == null) {
            if (!request.actionMethod.equalsIgnoreCase("start")) {
                Logger.debug("wizardFixPage Redirect Application.index, requested %s, currentUser %s, statusUser %d",
                        request.actionMethod, Secure.getCurrentUser(), status.getCurrentUserId());
                Application.index();
            }
        } else {
            if (!(request.action.equalsIgnoreCase(neededAction))) {
                Logger.debug("wizardFixPage REDIRECT Action %s TO NeededAction %s", request.action, neededAction);
                redirect(Router.getFullUrl(neededAction));
            }
        }
    }

    static public class BillDepositData {

        transient public LgUser currentUser = Secure.getCurrentUser();

        final public Boolean showReference1 = Configuration.mustShowBillDepositReference1();
        final public Boolean showReference2 = Configuration.mustShowBillDepositReference2();
        @CheckWith(FormDepositUserCodeBillReference.Validate.class)
        public FormDepositUserCodeBillReference reference1 = new FormDepositUserCodeBillReference();
        //@Required(message = "validation.required.reference2")
        public String reference2 = null;
        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = new FormCurrency();

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", currency=" + currency + '}';
        }
    }

    public static void start(@Valid BillDepositData formData) throws Throwable {
        if (!Configuration.isIgnoreBag() && !ModelFacade.isBagReady(false)) {
            Application.index();
        }

        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findEnabled();
        List<Currency> currencies = Currency.findEnabled();

        if (currencies.size() == 1
                && (referenceCodes.size() <= 1 || !Configuration.mustShowBillDepositReference1())
                && !Configuration.mustShowBillDepositReference2()) {
            // Nothing to complete, navigte to next step
            BillDeposit refDeposit = new BillDeposit(formData.currentUser, currencies.get(0), "", null);
            if (ModelFacade.startBillDepositAction(refDeposit)) {
                mainLoop();
            } else {
                ErrorController.onError();
            }
            return;
        }
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                Integer reference1 = (formData.reference1.lov == null ? null : formData.reference1.lov.lovId);
                if (formData.currency.currency == null) {
                    formData.currency.currency = Currency.findByNumericId(Configuration.getDefaultCurrency());
                }
                BillDeposit refDeposit = new BillDeposit(formData.currentUser, formData.currency.currency, formData.reference2, reference1);
                if (ModelFacade.startBillDepositAction(refDeposit)) {
                    mainLoop();
                } else {
                    ErrorController.onError();
                }
                return;
            }
        }
        if (formData == null) {
            formData = new BillDepositData();
            formData.currency.value = Configuration.getDefaultCurrency();
        }
        renderArgs.put("formData", formData);
        renderArgs.put("referenceCodes", referenceCodes);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        Object[] o;
        if (status instanceof MachineStatusError) {
            MachineStatusError err = (MachineStatusError) status;
            o = new Object[5];
            o[0] = err.getStateName();
            o[4] = "ERROR : " + err.getError();
        } else if (status instanceof MachineBillDepositStatus) {
            MachineBillDepositStatus billStatus = (MachineBillDepositStatus) status;
            o = new Object[5];
            o[0] = billStatus.getStateName();
            o[1] = billStatus.getBillQuantities();
            o[2] = billStatus.getCurrentSum();
            o[3] = billStatus.getTotalSum();
            o[4] = Messages.get("message." + billStatus.getStateName().toLowerCase());
            renderArgs.put("billData", billStatus.getBillQuantities());
            renderArgs.put("currentDeposit", billStatus.getCurrentDeposit());
            renderArgs.put("currentSum", billStatus.getCurrentSum());
            renderArgs.put("totalSum", billStatus.getTotalSum());
        } else {
            o = new Object[1];
            o[0] = "None";
        }

        if (request.isAjax()) {
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("showReference1", Configuration.mustShowBillDepositReference1());
            renderArgs.put("showReference2", Configuration.mustShowBillDepositReference2());
            render();
        }
    }

    public static void cancel() {
        ModelFacade.cancel();
        renderJSON("");
    }

    public static void accept() {
        ModelFacade.accept();
        renderJSON("");
    }

    public static void cancelTimeout() {
        ModelFacade.canceTimeout();
        renderJSON("");
    }

    public static void finish() {
        MachineBillDepositStatus billStatus = (MachineBillDepositStatus) status;
        BillDeposit deposit = billStatus.getCurrentDeposit();
        if (deposit == null) {
            Application.index();
            return;
        }
        if (!deposit.isFinished()) {
            mainLoop();
        }
        renderArgs.put("clientCode", Configuration.getClientDescription());
        renderArgs.put("user", Secure.getCurrentUser());
        renderArgs.put("providerCode", Configuration.getProviderDescription());
        renderArgs.put("currentDeposit", deposit);
        renderArgs.put("finishCause", deposit.finishCause);
        renderArgs.put("depositTotal", billStatus.getTotalSum());
        renderArgs.put("depositId", deposit.depositId);
        renderArgs.put("showReference1", Configuration.mustShowBillDepositReference1());
        renderArgs.put("showReference2", Configuration.mustShowBillDepositReference2());
        ModelFacade.confirmAction();
        render();
    }
}
