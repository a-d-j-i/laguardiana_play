package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import devices.device.DeviceEvent;
import devices.device.status.DeviceStatusClassCounterIntreface;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskCollect;
import devices.device.task.DeviceTaskCount;
import devices.device.task.DeviceTaskEnvelopeDeposit;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskStoringErrorReset;
import devices.device.task.DeviceTaskWithdraw;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import machines.MachineDeviceDecorator;
import models.ModelFacade;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With({Secure.class})
public class DeviceController extends Controller {

    static MachineDeviceDecorator device;
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
            hasClass = true;
            switch ((LgDevice.DeviceType) device.getType()) {
                case GLORY_DE50:
                case MEI_EBDS:
                    renderArgs.put("classCounter", true);
                    break;
                case IO_BOARD_MX220_1_0:
                case IO_BOARD_V4520_1_0:
                case IO_BOARD_V4520_1_2:
                    renderArgs.put("classIoBoard", true);
                    break;
                case OS_PRINTER:
                    renderArgs.put("classPrinter", true);
                    break;
                default:
                    hasClass = false;
                    break;

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
        Logger.debug("data %s : %s -> %s", device, property, value);
        LgDeviceProperty newProp = device.getProperty(property);
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
                        device.setProperty(property, "false");
                    } else if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        device.setProperty(property, "true");
                    } else {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case INTEGER:
                    try {
                        Integer i = Integer.parseInt(value);
                        device.setProperty(property, i.toString());
                    } catch (NumberFormatException e) {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case STRING:
                    device.setProperty(property, value);
                    break;
                case NOT_EDITABLE:
                default:
                    ret[2] = String.format("Property %s not editable", property);
                    break;
            }
            renderJSON(ret);
        }
    }

    static String error = null;

    // Counter Class
    public static void counterClassCommands(Integer deviceId, Integer currency) {
        Map<String, Integer> current = new HashMap<String, Integer>();
        Map<String, Integer> desired = new HashMap<String, Integer>();
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

        List<String> slots = new ArrayList<String>();
        slots.addAll(current.keySet());
        slots.addAll(desired.keySet());
        for (String s : slots) {
            if (current.get(s) == null) {
                current.put(s, 0);
            }
            if (desired.get(s) == null) {
                desired.put(s, 0);
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
        if (!device.submit(new DeviceTaskCount()).get()) {
            error = "Still executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void cancelDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskCancel()).get()) {
            error = "Cant cancel";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storeDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        int sequenceNumber = 1;
        if (!device.submit(new DeviceTaskStore(sequenceNumber)).get()) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void withdrawDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskWithdraw()).get()) {
            error = "Not counting cant store";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void reset(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskReset()).get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void storingErrorReset(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskStoringErrorReset()).get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void collectBag(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskCollect()).get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

    public static void envelopeDeposit(Integer deviceId, Integer currency) throws InterruptedException, ExecutionException {
        error = null;
        if (!device.submit(new DeviceTaskEnvelopeDeposit()).get()) {
            error = "Executing another command";
        }
        counterClassCommands(deviceId, currency);
    }

}
