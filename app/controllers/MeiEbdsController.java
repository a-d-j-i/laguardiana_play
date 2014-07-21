package controllers;

import devices.device.DeviceEvent;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.task.MeiEbdsTaskCount;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import machines.MachineDeviceDecorator;
import models.db.LgDevice;
import models.db.LgDeviceSlot;
import models.lov.Currency;
import play.mvc.Before;

public class MeiEbdsController extends Application {

    static MachineDeviceDecorator meiDevice;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        DeviceController.getCounter(deviceId);
        if (DeviceController.device.getType() == LgDevice.DeviceType.MEI_EBDS) {
            meiDevice = DeviceController.device;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    public static void count(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "count");
        Map<String, Integer> slots = new HashMap<String, Integer>();
        // $?
        Currency c = Currency.findById(1);
        for (LgDeviceSlot s : LgDeviceSlot.find(c, meiDevice.getLgDevice())) {
            slots.put(s.slot, null);
        }
        getStatus(deviceId, meiDevice.submit(new MeiEbdsTaskCount(slots)).get());
    }

    public static void store(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "store");
        getStatus(deviceId, meiDevice.submit(new DeviceTaskStore(1)).get());
    }

    public static void reject(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "reject");
        getStatus(deviceId, meiDevice.submit(new DeviceTaskWithdraw()).get());
    }

    public static void cancel(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "cancel");
        getStatus(deviceId, meiDevice.submit(new DeviceTaskCancel()).get());
    }

    public static void resetDevice(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "resetDevice");
        getStatus(deviceId, meiDevice.submit(new DeviceTaskReset()).get());
    }

    // Counter Class end
    public static void getStatus(Integer deviceId) {
        getStatus(deviceId, true);
    }

    public static void getStatus(Integer deviceId, boolean retval) {
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");
        DeviceEvent de = meiDevice.getLastEvent();
        String lastEvent = "";
        if (de != null) {
            lastEvent = de.toString();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[1];
            ret[ 0] = lastEvent;
            renderJSON(ret);
        } else {
            renderArgs.put("deviceId", deviceId);
            renderArgs.put("device", meiDevice);
            renderArgs.put("lastEvent", lastEvent);
            //renderArgs.put("backUrl", flash.get("backUrl"));
            render("DeviceController/" + meiDevice.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }

}
