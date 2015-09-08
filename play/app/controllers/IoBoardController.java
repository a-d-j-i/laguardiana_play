package controllers;

import static controllers.DeviceController.device;
import devices.device.DeviceEvent;
import devices.device.task.DeviceTaskReset;
import devices.ioboard.response.IoboardStatusResponse;
import devices.ioboard.task.IoboardTaskAproveBag;
import devices.ioboard.task.IoboardTaskCloseGate;
import devices.ioboard.task.IoboardTaskConfirmBag;
import devices.ioboard.task.IoboardTaskGetSensorStatus;
import devices.ioboard.task.IoboardTaskOpenGate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import machines.MachineDeviceDecorator;
import models.db.LgDevice;
import play.Logger;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;
import play.templates.TemplateLoader;

public class IoBoardController extends Application {

    static MachineDeviceDecorator ioBoard;

    @Before
    static void getBoard(Integer deviceId) throws Throwable {
        DeviceController.getCounter(deviceId);
        if (DeviceController.device.getDeviceType() == LgDevice.DeviceType.IO_BOARD_MEI_1_0) {
            ioBoard = DeviceController.device;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    public static void openGate(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "openGate");
        getStatus(deviceId, ioBoard.submit(new IoboardTaskOpenGate()).get());
    }

    public static void closeGate(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "closeGate");
        getStatus(deviceId, ioBoard.submit(new IoboardTaskCloseGate()).get());
    }

    public static void aproveBag(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "aproveBag");
        getStatus(deviceId, ioBoard.submit(new IoboardTaskAproveBag()).get());
    }

    public static void aproveBagConfirm(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "aproveBagConfirm");
        getStatus(deviceId, ioBoard.submit(new IoboardTaskConfirmBag()).get());
    }

    public static void clearError(Integer deviceId) throws InterruptedException, ExecutionException {
        flash.put("lastCmd", "clearError");
        getStatus(deviceId, ioBoard.submit(new DeviceTaskReset()).get());
    }

    public static void getStatus(Integer deviceId, boolean retval) throws InterruptedException, ExecutionException {
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");

        DeviceEvent de = ioBoard.getLastEvent();
        String lastEvent = "";
        if (de != null) {
            lastEvent = de.toString();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[0] = lastEvent;
            try {
                IoboardTaskGetSensorStatus deviceTask = new IoboardTaskGetSensorStatus();
                ioBoard.submit(deviceTask).get(2000, TimeUnit.MILLISECONDS);
                IoboardStatusResponse res = deviceTask.getSensorStatus();
                ret[1] = res;
                ret[2] = (res == null ? null : res.toString());
            } catch (TimeoutException ex) {
                ret[1] = null;
                ret[2] = "Timeout";
            }
            renderJSON(ret);
        } else {
            renderArgs.put("deviceId", deviceId);
            renderArgs.put("lastEvent", lastEvent);
            //renderArgs.put("backUrl", flash.get("backUrl"));
            String t = "DeviceController/" + device.getDeviceType().name().toUpperCase() + "_OPERATIONS.html";
            try {
                TemplateLoader.load(t);
                render(t);
            } catch (TemplateNotFoundException ex) {
                renderArgs.put(device.getDeviceType().getDeviceClass().name(), true);
                render("DeviceController/ioBoardClass.html");
            }
        }
    }
}
