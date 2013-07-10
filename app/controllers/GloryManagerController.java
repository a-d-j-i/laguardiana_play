package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import devices.glory.manager.ManagerInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.ModelFacade;
import models.db.LgBillType;
import play.Logger;
import play.mvc.Before;

// TODO: Manage errors.
public class GloryManagerController extends Application {

    static ManagerInterface manager;
    static ManagerInterface.MANAGER_STATE status = ManagerInterface.MANAGER_STATE.ERROR;
    static String error = null;
    static String success = null;

    @Before
    static void getManager() throws Throwable {
        manager = ModelFacade.getGloryManager();
        if (manager == null) {
            error = "Manager error opening port";
        } else {
            success = manager.getStatus().name();
            if (manager.getStatus().getState() == ManagerInterface.MANAGER_STATE.ERROR) {
                error = manager.getStatus().getError().toString();
            } else {
                error = null;
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
        if (request.isAjax()) {
            Object[] o = new Object[4];
            o[0] = error;
            o[1] = success;
            o[2] = ModelFacade.getBillQuantities();
            o[3] = currency;
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        }

        renderArgs.put("status", success);
        renderArgs.put("error", error);
        renderArgs.put("billData", ModelFacade.getBillQuantities());
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
            manager.cancelCommand();
        } else {
            Logger.error("MANAGER IS NULL");
        }
        index();
    }

    public static void collectBag() throws IOException {
        if (manager != null) {
            if (!manager.collect()) {
                error = "Cant collect";
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

    public static void withdrawDeposit() throws IOException {
        if (manager != null) {
            int sequenceNumber = 1;
            if (!manager.withdrawDeposit()) {
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
