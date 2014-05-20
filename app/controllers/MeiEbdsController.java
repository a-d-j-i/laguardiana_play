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
    
    private static void setStatusAndRedirect(Integer deviceId, boolean retVal) {
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("lastCmdRetVal", retVal);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getName().toUpperCase() + "_OPERATIONS.html");
    }
    
    public static void count(Integer deviceId) {
        Integer[] slotInfo = {1, 1, 1, 1, 1, 1, 1, 1};
        setStatusAndRedirect(deviceId, meiDevice.count(Arrays.asList(slotInfo)));
    }
    
    public static void store(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.storeDeposit(1));
    }
    
    public static void reject(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.reject());
    }
    
    public static void cancel(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.cancelDeposit());
    }
    
    public static void resetDevice(Integer deviceId) {
        setStatusAndRedirect(deviceId, meiDevice.reset());
    }
    
    public static void getStatus(Integer deviceId) {
    }
    
}
