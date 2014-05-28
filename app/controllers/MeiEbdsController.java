package controllers;

import devices.device.DeviceInterface;
import devices.mei.MeiEbdsDevice;
import java.util.Arrays;
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
            setStatusAndRedirect(deviceId, true);
        }
    }

    public static void count(Integer deviceId) {
        Integer[] slotInfo = {1, 1, 1, 1, 1, 1, 1, 1};
        setStatusAndRedirect(deviceId, meiDevice.count(Arrays.asList(slotInfo)));
    }

    public static void store(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.storeDeposit(1));
    }

    public static void reject(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.withdrawDeposit());
    }

    public static void cancel(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.cancelDeposit());
    }

    public static void resetDevice(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.reset());
    }

    public static void getStatus(Integer deviceId) {
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[ 0] = d.getLastEvent();
            renderJSON(ret);
        } else {
            renderArgs.put("deviceId", deviceId);
            renderArgs.put("device", d);
            renderArgs.put("lastEvent", d.getLastEvent());
            //renderArgs.put("backUrl", flash.get("backUrl"));
            render("DeviceController/" + d.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }

    private static void setStatusAndRedirect(Integer deviceId, boolean retVal) {
        renderArgs.put("lastCmdRetVal", retVal);
        getStatus(deviceId);
    }

}
