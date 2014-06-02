package controllers;

import devices.device.DeviceEvent;
import devices.device.DeviceInterface;
import devices.mei.MeiEbdsDevice;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import machines.Machine;
import play.mvc.Before;

public class MeiEbdsController extends Application {

    static MeiEbdsDevice meiDevice;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        if (deviceId == null) {
            DeviceController.list();
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d instanceof MeiEbdsDevice) {
            meiDevice = (MeiEbdsDevice) d;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    public static void count(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "count");
        Integer[] slotInfo = {1, 1, 1, 1, 1, 1, 1, 1};
        getStatus(deviceId, meiDevice.count(Arrays.asList(slotInfo)));
    }

    public static void store(Integer deviceId) {
        flash.put("lastCmd", "store");
        getStatus(deviceId, meiDevice.storeDeposit(1));
    }

    public static void reject(Integer deviceId) {
        flash.put("lastCmd", "reject");
        getStatus(deviceId, meiDevice.withdrawDeposit());
    }

    public static void cancel(Integer deviceId) {
        flash.put("lastCmd", "cancel");
        getStatus(deviceId, meiDevice.cancelDeposit());
    }

    public static void resetDevice(Integer deviceId) {
        flash.put("lastCmd", "resetDevice");
        getStatus(deviceId, meiDevice.reset());
    }

    public static void getStatus(Integer deviceId) {
        getStatus(deviceId, true);
    }

    public static void getStatus(Integer deviceId, boolean retval) {
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");
        DeviceInterface d = Machine.findDeviceById(deviceId);
        DeviceEvent de = d.getLastEvent();
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
            renderArgs.put("device", d);
            renderArgs.put("lastEvent", lastEvent);
            //renderArgs.put("backUrl", flash.get("backUrl"));
            render("DeviceController/" + d.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }

}
