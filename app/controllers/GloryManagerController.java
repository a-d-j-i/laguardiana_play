package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import devices.glory.manager.ManagerInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.ModelFacade;
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

    public static void index(Integer currency) {
        if (error != null) {
            Logger.error(error);
        }

        if (currency == null) {
            currency = 1;
        }

        Map<Integer, Integer> current = manager.getCurrentQuantity();
        Map<Integer, Integer> desired = manager.getDesiredQuantity();
        Set<Integer> slots = new HashSet<Integer>();
        if (current == null) {
            current = new HashMap<Integer, Integer>();
        }
        if (desired == null) {
            desired = new HashMap<Integer, Integer>();
        }
        slots.addAll(current.keySet());
        slots.addAll(desired.keySet());
        for (int i = 0; i < 32; i++) {
            slots.add(i);
            if (current.get(i) == null) {
                current.put(i, 0);
            }
            if (desired.get(i) == null) {
                desired.put(i, 0);
            }
        }
        if (request.isAjax()) {
            Object[] o = new Object[10];
            o[0] = error;
            o[1] = success;
            o[2] = currency;
            o[3] = slots.toArray();
            o[4] = current;
            o[5] = desired;
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        }

        renderArgs.put("status", success);
        renderArgs.put("error", error);

        renderArgs.put("slots", slots);
        renderArgs.put("current", current);
        renderArgs.put("desired", desired);

        List<Integer> currencyList = new ArrayList<Integer>();
        for (int i = 0; i < 8; i++) {
            currencyList.add(i);
        }
        renderArgs.put("currencyList", currencyList);
        renderArgs.put("currency", currency);

        render();
    }

    public static void count(Map<String, String> slotsIds, Integer currency) throws IOException {
        if (currency == null || currency == 0) {
            currency = 1;
        }
        if (manager != null) {
            Map<Integer, Integer> desiredQuantity = new HashMap< Integer, Integer>();

            if (slotsIds != null) {
                for (String k : slotsIds.keySet()) {
                    Integer slot = Integer.parseInt(k);
                    if (slot != null && slot >= 32) {
                        Logger.error(String.format("getSlotArray Invalid slot %d", slot));
                    } else {
                        desiredQuantity.put(slot, Integer.parseInt(slotsIds.get(k)));
                    }
                }
            }
            Logger.debug("--------> %s", desiredQuantity);
            if (!manager.count(desiredQuantity, currency)) {
                error = "Still executing another command";
            }
        }

        index(currency);
    }

    public static void cancelDeposit(Integer currency) throws IOException {
        if (manager != null) {
            manager.cancelCommand();
        } else {
            Logger.error("MANAGER IS NULL");
        }
        index(currency);
    }

    public static void collectBag(Integer currency) throws IOException {
        if (manager != null) {
            if (!manager.collect()) {
                error = "Cant collect";
            }
        }
        index(currency);
    }

    public static void storeDeposit(Integer currency) throws IOException {
        if (manager != null) {
            int sequenceNumber = 1;
            if (!manager.storeDeposit(sequenceNumber)) {
                error = "Not counting cant store";
            }
        }
        index(currency);
    }

    public static void withdrawDeposit(Integer currency) throws IOException {
        if (manager != null) {
            int sequenceNumber = 1;
            if (!manager.withdrawDeposit()) {
                error = "Not counting cant store";
            }
        }
        index(currency);
    }

    public static void reset(Integer currency) throws IOException {
        if (manager != null) {
            if (!manager.reset()) {
                error = "Executing another command";
            }
        }
        index(currency);
    }

    public static void storingErrorReset(Integer currency) throws IOException {
        if (manager != null) {
            if (!manager.storingErrorReset()) {
                error = "Executing another command";
            }
        }
        index(currency);
    }

    public static void envelopeDeposit(Integer currency) throws IOException {
        if (manager != null) {
            if (!manager.envelopeDeposit()) {
                error = "Executing another command";
            }
        }
        index(currency);
    }
}
