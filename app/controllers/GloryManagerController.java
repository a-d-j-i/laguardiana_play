package controllers;

import devices.glory.manager.Manager;
import java.io.IOException;
import java.util.Map;
import devices.CounterFactory;
import java.util.HashMap;
import java.util.List;
import models.Bill;
import models.db.LgBillType;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

// TODO: Manage errors.
public class GloryManagerController extends Controller {

    static Manager.ControllerApi manager;
    static String error = null;
    static String success = null;

    @Before
    static void getManager() throws Throwable {
        manager = CounterFactory.getManager(Play.configuration.getProperty("glory.port"));
        if (manager == null) {
            error = "Manager error opening port";
        } else {
            success = manager.getSuccess();
            error = manager.getError();
        }
    }

    public static void index() {
        if (success != null) {
            Logger.info(success);
        }
        if (error != null) {
            Logger.error(error);
        }

        List<Bill> billData = Bill.getCurrentCounters();

        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = error;
            o[1] = success;
            o[2] = billData;
            renderJSON(o);
        }

        renderArgs.put("billData", billData);
        render();
    }

    public static void count(Map<String, String> billTypeIds) throws IOException {
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
            if (!manager.count(desiredQuantity)) {
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
