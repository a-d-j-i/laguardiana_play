package controllers;

import java.util.List;
import models.ModelFacade;
import models.lov.Currency;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Before;
import validation.FormCurrencyBills;

public class FilterController extends Application {

    @Before
    static void wizardFixPage() throws Throwable {
        switch (modelFacade.getCurrentStep()) {
            case NONE:
                if (!request.actionMethod.equalsIgnoreCase("start")) {
                    Application.index();
                    break;
                }
                break;
            case FINISH:
            case RUNNING:
                if (modelFacade.getCurrentMode() != ModelFacade.CurrentMode.COUNTING) {
                    Application.index();
                }
                break;
            default:
                Application.index();
                break;
        }
    }

    static public class FormData {

        @CheckWith(FormCurrencyBills.Validate.class)
        public FormCurrencyBills currency = null;
        public Integer billQuantity = null;
        public Integer batchQuantity = null;

        @Override
        public String toString() {
            return "FormData{" + "currency=" + currency + '}';
        }
    }

    public static void start(@Valid FormData formData)
            throws Throwable {
        Logger.debug("start data %s", formData);
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("start : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                modelFacade.startCounting(formData, formData.currency.currency);
                mainLoop();
                return;
            }
        }
        if (formData == null) {
            formData = new FormData();
        }

        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        if (request.isAjax()) {
            Object[] o = new Object[2];
            o[0] = modelFacade.getCurrentStep();
            o[1] = modelFacade.getBillData();
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", modelFacade.getBillData());
            render();
        }
    }

    public static void cancel() {
        modelFacade.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        cancel();
    }

    public static void finish() {
        modelFacade.finishDeposit();
        Application.index();
    }
}
