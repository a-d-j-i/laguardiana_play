package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import java.util.List;
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
import play.mvc.Router;
import validation.FormCurrency;
import validation.FormDepositUserCodeBillReference;

public class BillDepositController extends ErrorController {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        if (request.isAjax()) {
            return;
        }
        status = ModelFacade.getStateStatus();
        String neededAction = status.getNeededAction();
        String neededController = status.getNeededController();
        if (neededAction == null || neededController == null) {
            BillDepositData data = (BillDepositData) status.getFormData();
            if (!request.actionMethod.equalsIgnoreCase("start") || data.currentUser != Secure.getCurrentUser()) {
                Logger.debug("wizardFixPage Redirect Application.index");
                Application.index();
            }
        } else {
            if (!(request.controller.equalsIgnoreCase(neededController))) {
                Logger.debug("wizardFixPage REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
                redirect(Router.getFullUrl(neededController + "." + neededAction));
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

        public Currency getCurrency() {
            return currency.currency;
        }

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", currency=" + currency + '}';
        }
    }

    public static void start(@Valid BillDepositData formData) throws Throwable {
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();

        if (currencies.size() == 1
                && (referenceCodes.size() <= 1 || !Configuration.mustShowBillDepositReference1())
                && !Configuration.mustShowBillDepositReference2()) {
            // Nothing to complete, navigte to next step
            formData = new BillDepositData();
            formData.currency = new FormCurrency(currencies.get(0));
            if (ModelFacade.startBillDepositAction(formData)) {
                mainLoop();
            } else {
                Application.index();
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
                if (ModelFacade.startBillDepositAction(formData)) {
                    mainLoop();
                } else {
                    Application.index();
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
        BillDepositData data = (BillDepositData) status.getFormData();
        BillDeposit d = (BillDeposit) status.getDeposit();
        long totalSum = 0;
        if (d != null) {
            totalSum = d.getTotal();
        }
        if (request.isAjax()) {
            Object[] o = new Object[4];
            o[0] = status.getState();
            o[1] = ModelFacade.getBillQuantities(data.currency.value);
            o[2] = Messages.get(status.getActionMessage());
            o[3] = totalSum;
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        } else {
            /*            long currentTotalSum = totalSum;
             for (BillDAO b : billList) {
             currentTotalSum += (b.d * b.q);
             }*/
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("billData", ModelFacade.getBillQuantities(data.currency.value));
            renderArgs.put("formData", status.getFormData());
            renderArgs.put("totalSum", totalSum);
            //renderArgs.put("currentTotalSum", currentTotalSum);
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
        BillDeposit deposit = (BillDeposit) status.getDeposit();
        BillDepositData formData = (BillDepositData) status.getFormData();
        if (formData == null) {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", Configuration.getClientDescription());
        renderArgs.put("user", Secure.getCurrentUser());
        renderArgs.put("providerCode", Configuration.getProviderDescription());
        renderArgs.put("formData", formData);
        if (deposit != null) {
            Long total = deposit.getTotal();
            renderArgs.put("finishCause", deposit.finishCause);
            renderArgs.put("depositTotal", total);
            renderArgs.put("depositId", deposit.depositId);
        }
        ModelFacade.finishAction();
        render();
    }
}
