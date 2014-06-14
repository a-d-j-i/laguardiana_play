package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceClassIoBoardInterface;
import devices.device.DeviceClassPrinterInterface;
import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.status.DeviceStatusClassCounterIntreface;
import devices.device.status.DeviceStatusInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import models.ModelFacade;
import models.db.LgDeviceProperty;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With({Secure.class})
public class DeviceController extends Controller {

    static DeviceInterface device;
    static boolean hasClass = false;
    static String deviceName;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        if (deviceId != null) {
            device = ModelFacade.findDeviceById(deviceId);
            if (device == null) {
                list();
            }
            deviceName = device.getType().name();
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
            renderArgs.put("deviceId", deviceId);
        }
    }

    public static void list() {
        renderArgs.put("data", ModelFacade.getDevices());
        render();
    }

    public static void commands(Integer deviceId) {
        if (hasClass) {
            render("DeviceController/deviceClass.html");
        } else {
            operations(deviceId);
        }
    }

    public static void operations(Integer deviceId) {
        render("DeviceController/" + deviceName.toUpperCase() + "_OPERATIONS.html"
        );
    }

    public static void printerClassCommands(Integer deviceId) {
        render("DeviceController/printerClass.html");
    }

    public static void ioBoardClassCommands(Integer deviceId) {
        render("DeviceController/ioBoardClass.html");
    }

    // TODO: Enforce security.
    public static void properties(Integer deviceId) {
        List<LgDeviceProperty> data = device.getEditableProperties();
        /*for (LgDeviceProperty p : data) {
         //boolean perm = Secure.checkPermission(p.name, "GET");
         if (!perm) {
         data.remove(p);
         }
         }*/
        Logger.debug("data %s -> %s", device, data);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("data", data);
        renderArgs.put("device", device);
        render();
    }

    public static void setProperty(Integer deviceId, String property, String value) throws InterruptedException, ExecutionException {
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
        DeviceInterface d = ModelFacade.findDeviceById(deviceId);
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
        if (!device.clearError()) {
            error = "can't clear error now";
        }
        counterClassCommands(deviceId, null);
    }

    static String error = null;

    // Counter Class
    public static void counterClassCommands(Integer deviceId, Integer currency) {
        Map<Integer, Integer> current = new HashMap<Integer, Integer>();
        Map<Integer, Integer> desired = new HashMap<Integer, Integer>();
        DeviceStatusInterface st = null;
        DeviceEvent event = device.getLastEvent();
        if (event != null) {
            st = event.getStatus();
            if (st instanceof DeviceStatusClassCounterIntreface) {
                DeviceStatusClassCounterIntreface status = (DeviceStatusClassCounterIntreface) st;
                current = status.getCurrentQuantity();
                desired = status.getDesiredQuantity();
            }
        }

        if (currency == null) {
            currency = 1;
        }

        List<Integer> slots = new ArrayList<Integer>();
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
            o[1] = st;
            o[2] = currency;
            o[3] = slots.toArray();
            o[4] = current;
            o[5] = desired;
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        }

        renderArgs.put("status", st);
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

    public static void count(Integer deviceId, Map<String, String> slotsIds, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (currency == null || currency == 0) {
            currency = 1;
        }
        Map<String, Integer> desiredQuantity = new HashMap< String, Integer>();

        if (slotsIds != null) {
            for (String k : slotsIds.keySet()) {
                desiredQuantity.put(k, Integer.parseInt(slotsIds.get(k)));
            }
        }
        Logger.debug("--------> %s", desiredQuantity);
        if (!counter.count(currency, desiredQuantity).get()) {
            error = "Still executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void cancelDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.cancelDeposit().get()) {
            error = "Cant cancel";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storeDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        int sequenceNumber = 1;
        if (!counter.storeDeposit(sequenceNumber).get()) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void withdrawDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.withdrawDeposit().get()) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void reset(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.errorReset().get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storingErrorReset(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.storingErrorReset().get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void collectBag(Integer deviceId, Integer currency) {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        counter.collect();
        counterClassCommands(deviceId, currency);
    }

    public static void envelopeDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        DeviceClassCounterIntreface counter = (DeviceClassCounterIntreface) device;
        if (!counter.envelopeDeposit().get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

}
