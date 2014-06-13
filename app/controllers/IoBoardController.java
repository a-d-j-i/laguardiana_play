package controllers;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.ioboard.IoBoard;
import java.io.IOException;
import models.ModelFacade;
import play.mvc.Before;

public class IoBoardController extends Application {

    static IoBoard ioBoard;

    @Before
    static void getBoard(Integer deviceId) throws Throwable {
        DeviceController.getCounter(deviceId);
        if (DeviceController.device instanceof IoBoard) {
            ioBoard = (IoBoard) DeviceController.device;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    public static void openGate(Integer deviceId) throws IOException {
        flash.put("lastCmd", "openGate");
        getStatus(deviceId, ioBoard.openGate());
    }

    public static void closeGate(Integer deviceId) {
        flash.put("lastCmd", "closeGate");
        getStatus(deviceId, ioBoard.closeGate());
    }

    public static void aproveBag(Integer deviceId) throws IOException {
        flash.put("lastCmd", "aproveBag");
        getStatus(deviceId, ioBoard.aproveBag());
    }

    public static void aproveBagConfirm(Integer deviceId) {
        flash.put("lastCmd", "aproveBagConfirm");
        getStatus(deviceId, ioBoard.aproveBagConfirm());
    }

    public static void clearError(Integer deviceId) {
        flash.put("lastCmd", "clearError");
        getStatus(deviceId, ioBoard.reset());
    }

    public static void getStatus(Integer deviceId, boolean retval) {
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");
        DeviceInterface d = ModelFacade.findDeviceById(deviceId);
        DeviceEvent de = d.getLastEvent();
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
            renderArgs.put("device", d);
            renderArgs.put("lastEvent", lastEvent);
            //renderArgs.put("backUrl", flash.get("backUrl"));
            render("DeviceController/" + d.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }
}
