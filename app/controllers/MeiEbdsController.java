package controllers;

import devices.device.DeviceInterface;
import devices.mei.MeiEbdsDevice;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsg;
import machines.Machine;
import play.Logger;
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

    private static void setStatusAndRedirect(Integer deviceId, boolean retVal) {
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("lastCmdRetVal", retVal);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getName().toUpperCase() + "_OPERATIONS.html");
    }

    public static void store(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.store());
    }

    public static void resetDevice(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.reset());
    }

    public static void getStatus(Integer deviceId) {
    }

}
