package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Bill;
import models.db.LgBillType;
import play.Logger;
import play.Play;
import play.mvc.Before;

// TODO: Manage errors.
public class GloryManagerController extends Application {

    static Manager.ControllerApi manager;
    static Manager.Status status = Manager.Status.ERROR;
    static String error = null;
    static String success = null;

    @Before
    static void getManager() throws Throwable {
        manager = CounterFactory.getManager(Play.configuration.getProperty("glory.port"));
        if (manager == null) {
            error = "Manager error opening port";
        } else {
            if (manager.getStatus() == Manager.Status.ERROR) {
                error = "ERROR " + manager.getErrorDetail().toString();
            } else {
                error = null;
                success = manager.getStatus().name();
            }
        }
    }

    public static void index() {
        if (error != null) {
            Logger.error(error);
        }

        Integer currency = 1;
        if (manager != null) {
            currency = manager.getCurrency();
            if (currency == null) {
                currency = 1;
            }
        }

        List<Bill> billData = Bill.getCurrentCounters(currency);
        if (request.isAjax()) {
            Object[] o = new Object[4];
            o[0] = error;
            o[1] = success;
            o[2] = billData;
            o[3] = currency;
            renderJSON(o);
        }

        renderArgs.put("billData", billData);
        renderArgs.put("currency", currency);
        List<Integer> currencyList = new ArrayList<Integer>();
        for (int i = 0; i < 8; i++) {
            currencyList.add(i);
        }
        renderArgs.put("currencyList", currencyList);
        render();
    }

    public static void count(Map<String, String> billTypeIds, Integer currency) throws IOException {
        if (currency == null || currency == 0) {
            index();
        }
        if (manager != null) {
            Map<Integer, Integer> desiredQuantity = new HashMap< Integer, Integer>();

            if (billTypeIds != null) {
                for (String k : billTypeIds.keySet()) {

                    LgBillType b = LgBillType.findById(Integer.parseInt(k));
                    if (b != null && b.slot > 0) {
                        if (b.slot >= 32) {
                            Logger.error(String.format("getSlotArray Invalid slot %d", b.slot));
                        }
                        desiredQuantity.put(b.slot, Integer.parseInt(billTypeIds.get(k)));
                    }
                }
            }
            if (!manager.count(desiredQuantity, currency)) {
                error = "Still executing another command";
            }
        }

        index();
    }

    public static void cancelDeposit() throws IOException {
        if (manager != null) {
            if (!manager.cancelDeposit()) {
                error = "Not counting cant cancel";
            }
        }
        index();
    }

    public static void storeDeposit() throws IOException {
        if (manager != null) {
            int sequenceNumber = 1;
            if (!manager.storeDeposit(sequenceNumber)) {
                error = "Not counting cant store";
            }
        }
        index();
    }

    public static void reset() throws IOException {
        if (manager != null) {
            if (!manager.reset()) {
                error = "Executing another command";
            }
        }
        index();
    }

    public static void storingErrorReset() throws IOException {
        if (manager != null) {
            if (!manager.storingErrorReset()) {
                error = "Executing another command";
            }
        }
        index();
    }

    public static void envelopeDeposit() throws IOException {
        if (manager != null) {
            if (!manager.envelopeDeposit()) {
                error = "Executing another command";
            }
        }
        index();
    }
}
