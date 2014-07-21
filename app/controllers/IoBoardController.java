package controllers;

import devices.device.DeviceEvent;
import java.io.IOException;
import machines.MachineDeviceDecorator;
import models.db.LgDevice;
import play.mvc.Before;

public class IoBoardController extends Application {

    static MachineDeviceDecorator ioBoard;

    @Before
    static void getBoard(Integer deviceId) throws Throwable {
        DeviceController.getCounter(deviceId);
        if (DeviceController.device.getType() == LgDevice.DeviceType.IO_BOARD_MX220_1_0
                || DeviceController.device.getType() == LgDevice.DeviceType.IO_BOARD_V4520_1_0
                || DeviceController.device.getType() == LgDevice.DeviceType.IO_BOARD_V4520_1_2) {
            ioBoard = DeviceController.device;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    public static void openGate(Integer deviceId) throws IOException {
        flash.put("lastCmd", "openGate");
//        getStatus(deviceId, ioBoard.openGate());
    }

    public static void closeGate(Integer deviceId) {
        flash.put("lastCmd", "closeGate");
//        getStatus(deviceId, ioBoard.closeGate());
    }

    public static void aproveBag(Integer deviceId) throws IOException {
        flash.put("lastCmd", "aproveBag");
//        getStatus(deviceId, ioBoard.aproveBag());
    }

    public static void aproveBagConfirm(Integer deviceId) {
        flash.put("lastCmd", "aproveBagConfirm");
//        getStatus(deviceId, ioBoard.aproveBagConfirm());
    }

    public static void clearError(Integer deviceId) {
        flash.put("lastCmd", "clearError");
 //       getStatus(deviceId, ioBoard.reset());
    }

    public static void getStatus(Integer deviceId, boolean retval) {
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");
        DeviceEvent de = ioBoard.getLastEvent();
        String lastEvent = "";
        if (de != null) {
            lastEvent = de.toString();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[1];
            ret[ 0] = lastEvent;
            //o[1] = ioBoard.getInternalState();
            renderJSON(ret);
        } else {
            renderArgs.put("deviceId", deviceId);
            renderArgs.put("device", ioBoard);
            renderArgs.put("lastEvent", lastEvent);
            //renderArgs.put("backUrl", flash.get("backUrl"));
            render("DeviceController/" + ioBoard.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }
}
