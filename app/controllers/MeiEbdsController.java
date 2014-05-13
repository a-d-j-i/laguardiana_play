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
            setStatusAndRedirect(deviceId, null, true);
        }
    }

    private static void setStatusAndRedirect(Integer deviceId, MeiEbdsAcceptorMsg st, boolean retVal) {
        if (st != null) {
            Logger.debug("STATUS : %s", st.toString());
            renderArgs.put("status", st);
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("lastCmdRetVal", retVal);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getName().toUpperCase() + "_OPERATIONS.html");
    }

    public static void startCounting(Integer deviceId) {
        //setStatusAndRedirect(deviceId, (MeiEbdsAcceptorMsg) meiDevice.sendOperation(new MeiEbdsHostMsg(), false), true);
    }

    public static void resetDevice(Integer deviceId) {
        setStatusAndRedirect(deviceId, null, meiDevice.reset());
    }

    public static void getStatus(Integer deviceId) {
    }

}
