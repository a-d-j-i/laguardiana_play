package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import devices.DeviceClassCounterIntreface;
import devices.DeviceClassIoBoardInterface;
import devices.DeviceClassPrinterInterface;
import devices.DeviceInterface;
import devices.DeviceStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import machines.Machine;
import models.db.LgDeviceProperty;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

@With({Secure.class})
public class DeviceController extends Controller {

    static DeviceInterface device = null;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        if (deviceId != null) {
            device = Machine.findDeviceById(deviceId);
            renderArgs.put("deviceId", deviceId);
        }
    }

    public static void list() {
        renderArgs.put("data", Machine.getDevices());
        render();
    }

    @Util
    static public boolean getDeviceArgs() {
        if (device == null) {
            list();
        }
        boolean hasClass = false;
        if (device instanceof DeviceClassIoBoardInterface) {
            hasClass = true;
            renderArgs.put("classIoBoard", true);
        }
        if (device instanceof DeviceClassPrinterInterface) {
            hasClass = true;
            renderArgs.put("classPrinter", true);
        }
        if (device instanceof DeviceClassCounterIntreface) {
            hasClass = true;
            renderArgs.put("classCounter", true);
        }
        renderArgs.put("device", device);
        renderArgs.put("hasClass", hasClass);
        return hasClass;
    }

    public static void commands(Integer deviceId) {
        if (getDeviceArgs()) {
            render("DeviceController/deviceClass.html");
        } else {
            operations(deviceId);
        }
    }

    public static void operations(Integer deviceId) {
        getDeviceArgs();
        render("DeviceController/" + device.getName().toUpperCase() + "_OPERATIONS.html");
    }

    public static void printerClassCommands(Integer deviceId) {
        getDeviceArgs();
        render("DeviceController/printerClass.html");
    }

    public static void ioBoardClassCommands(Integer deviceId) {
        getDeviceArgs();
        render("DeviceController/ioBoardClass.html");
    }

    // TODO: Enforce security.
    public static void properties(Integer deviceId) {
        if (deviceId == null) {
            list();
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d == null) {
            list();
        }
        List<LgDeviceProperty> data = d.getEditableProperties();
        /*for (LgDeviceProperty p : data) {
         //boolean perm = Secure.checkPermission(p.name, "GET");
         if (!perm) {
         data.remove(p);
         }
         }*/
        Logger.debug("data %s -> %s", d, data);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("data", data);
        renderArgs.put("device", d);
        render();
    }

    public static void setProperty(Integer deviceId, String property, String value) {
        if (!request.isAjax()) {
            list();
        }
        boolean perm = Secure.checkPermission(property, "SET");
        Object ret[] = new Object[3];
        ret[0] = null;
        ret[1] = null;
        if (!perm) {
            ret[2] = String.format("access denied for property %s", property);
            renderJSON(ret);
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d == null) {
            ret[2] = String.format("invalid device Id");
            renderJSON(ret);
        }
        Logger.debug("data %s : %s -> %s", d, property, value);
        LgDeviceProperty newProp = d.setProperty(property, value);
        if (newProp == null) {
            ret[2] = String.format("invalid property %s", property);
            renderJSON(ret);
        } else {
            ret[0] = newProp.devicePropertyId;
            ret[1] = newProp.value;
            ret[2] = false;
            switch (newProp.editType) {
                case BOOLEAN:
                    if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                        newProp.value = "false";
                        newProp.save();
                    } else if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        newProp.value = "true";
                        newProp.save();
                    } else {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case INTEGER:
                    try {
                        Integer i = Integer.parseInt(value);
                        newProp.value = i.toString();
                        newProp.save();
                    } catch (NumberFormatException e) {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case STRING:
                    newProp.value = value;
                    newProp.save();
                    break;
                case NOT_EDITABLE:
                default:
                    ret[2] = String.format("Property %s not editable", property);
                    break;
            }
            renderJSON(ret);
        }
    }

    public static void clearError(Integer deviceId) {
        error = null;
        getDeviceArgs();
        if (!device.clearError()) {
            error = "can't clear error now";
        }
        counterClassCommands(deviceId, null);
    }

    static String error = null;

    // Counter Class
    public static void counterClassCommands(Integer deviceId, Integer currency) {
        getDeviceArgs();
        String status = device.getStatus().getError();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;

        if (currency == null) {
            currency = 1;
        }

        Map<Integer, Integer> current = counter.getCurrentQuantity();
        Map<Integer, Integer> desired = counter.getDesiredQuantity();
        List<Integer> slots = new ArrayList<Integer>();
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
            o[1] = status;
            o[2] = currency;
            o[3] = slots.toArray();
            o[4] = current;
            o[5] = desired;
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        }

        renderArgs.put("status", status);
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

    public static void count(Integer deviceId, Map<String, String> slotsIds, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (currency == null || currency == 0) {
            currency = 1;
        }
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
        if (!counter.count(desiredQuantity, currency)) {
            error = "Still executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void cancelDeposit(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.cancelDeposit()) {
            error = "Cant cancel";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storeDeposit(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        int sequenceNumber = 1;
        if (!counter.storeDeposit(sequenceNumber)) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void withdrawDeposit(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.withdrawDeposit()) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void reset(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.reset()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storingErrorReset(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.storingErrorReset()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void collectBag(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        counter.collect();
        counterClassCommands(deviceId, currency);
    }

    public static void envelopeDeposit(Integer deviceId, Integer currency) {
        error = null;
        getDeviceArgs();
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.envelopeDeposit()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }
    // Counter Class end

}
