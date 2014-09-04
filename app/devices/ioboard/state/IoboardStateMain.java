package devices.ioboard.state;

import devices.device.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReadTimeout;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskResponse;
import devices.ioboard.IoboardDevice;
import static devices.ioboard.IoboardDevice.IOBOARD_MAX_RETRIES;
import devices.ioboard.response.IoboardCriticalResponse;
import devices.ioboard.response.IoboardErrorResponse;
import devices.ioboard.response.IoboardStateResponse;
import devices.ioboard.response.IoboardStatusResponse;
import devices.ioboard.status.IoboardStatus;
import devices.ioboard.status.IoboardStatusCriticalError;
import devices.ioboard.task.IoboardTaskAproveBag;
import devices.ioboard.task.IoboardTaskCloseGate;
import devices.ioboard.task.IoboardTaskConfirmBag;
import devices.ioboard.task.IoboardTaskGetSensorStatus;
import devices.ioboard.task.IoboardTaskOpenGate;
import play.Logger;

/**
 *
 * @author adji
 */
public class IoboardStateMain extends IoboardStateAbstract {

    protected void debug(String message, Object... args) {
        Logger.debug(message, args);
    }

    public IoboardStateMain(IoboardDevice ioboard) {
        super(ioboard);
    }

    private int retries = 0;
    private IoboardStatus.IoboardBagApprovedState bagAproveState = IoboardStatus.IoboardBagApprovedState.BAG_APROVED;
    private IoboardStateResponse lastStateResponse;
    private IoboardStatusResponse lastStatusResponse;
    private IoboardTaskGetSensorStatus pendingSensorStatusTask = null;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        //debug("IoboardStateMain received task %s", task.toString());
        boolean ret;
        if (task instanceof DeviceTaskReadTimeout) {
            task.setReturnValue(true);
            retries++;
            debug("RETRIES : %d", retries);

            if (retries == IOBOARD_MAX_RETRIES) {
                retries = 0;
                task.setReturnValue(true);
                if (pendingSensorStatusTask != null) {
                    pendingSensorStatusTask.setReturnValue(false);
                }
                return new IoboardError(ioboard, "Timeout reading from serial port");
            }
            ret = true;
        } else if (task instanceof IoboardTaskOpenGate) {
            Logger.debug("openGate");
            ioboard.sendCmd('O');
            ret = true;
        } else if (task instanceof IoboardTaskCloseGate) {
            Logger.debug("closeGate");
            ioboard.sendCmd('C');
            ret = true;
        } else if (task instanceof IoboardTaskAproveBag) {
            Logger.debug("aproveBag");
            bagAproveState = IoboardStatus.IoboardBagApprovedState.BAG_APROVE_WAIT;
            ioboard.sendCmd('A');
            ret = true;
        } else if (task instanceof IoboardTaskConfirmBag) {
            Logger.debug("aproveConfirmBag");
            bagAproveState = IoboardStatus.IoboardBagApprovedState.BAG_APROVED;
            ret = true;
        } else if (task instanceof DeviceTaskResponse) {
            retries = 0;
            DeviceTaskResponse tr = (DeviceTaskResponse) task;
            DeviceResponseInterface response = tr.getResponse();
            if (response instanceof IoboardStateResponse) {
                IoboardStateResponse r = (IoboardStateResponse) response;
                IoboardStatus.IoboardBagApprovedState prevBagAproveState = bagAproveState;
                switch (bagAproveState) {
                    case BAG_APROVED:
                        if (!r.isBagAproveState()) {
                            Logger.debug("IOBOARD BAG NOT APROVED");
                            bagAproveState = IoboardStatus.IoboardBagApprovedState.BAG_NOT_APROVED;
                        }
                        break;
                    case BAG_APROVE_WAIT:
                        if (r.isBagAproveState()) {
                            Logger.debug("IOBOARD BAG APROVE CONFIRM");
                            bagAproveState = IoboardStatus.IoboardBagApprovedState.BAG_APROVE_CONFIRM;
                        }
                        break;
                    case BAG_APROVE_CONFIRM:
                    case BAG_NOT_APROVED:
                        break;
                }
                if (lastStateResponse == null || !r.equals(lastStateResponse) || !bagAproveState.equals(prevBagAproveState)) {
                    ioboard.notifyListeners(new IoboardStatus(r, bagAproveState));
                }
                lastStateResponse = r;
            } else if (response instanceof IoboardErrorResponse) {
                IoboardErrorResponse r = (IoboardErrorResponse) response;
                ioboard.notifyListeners(new DeviceStatusError(r.getError()));
            } else if (response instanceof IoboardCriticalResponse) {
                IoboardCriticalResponse r = (IoboardCriticalResponse) response;
                ioboard.notifyListeners(new IoboardStatusCriticalError(r.getError()));
            } else if (response instanceof IoboardStatusResponse) {
                lastStatusResponse = (IoboardStatusResponse) response;
                if (pendingSensorStatusTask != null) {
                    pendingSensorStatusTask.setResponse(lastStatusResponse);
                    pendingSensorStatusTask = null;
                }
            }
            task.setReturnValue(true);
            return this;
        } else if (task instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
            if (ioboard.open(open.getPort())) {
                debug("%s IoboardStateMain new port %s", ioboard.toString(), open.getPort());
                task.setReturnValue(true);
                return this;
            } else {
                debug("%s IoboardStateMain new port %s failed to open", ioboard.toString(), open.getPort());
                task.setReturnValue(false);
                if (pendingSensorStatusTask != null) {
                    pendingSensorStatusTask.setReturnValue(false);
                }
                return new IoboardOpenPort(ioboard);
            }
        } else if (task instanceof IoboardTaskGetSensorStatus) {
            if (pendingSensorStatusTask == null) {
                pendingSensorStatusTask = (IoboardTaskGetSensorStatus) task;
                String err = ioboard.sendCmd('S');
                if (err != null) {
                    if (pendingSensorStatusTask != null) {
                        pendingSensorStatusTask.setReturnValue(false);
                    }
                    return new IoboardError(ioboard, err);
                }
            } else {
                task.setReturnValue(false);
            }
            return null;
        } else if (task instanceof DeviceTaskReset) {
            debug("CLEARING STATES");
            lastStateResponse = null;
            lastStatusResponse = null;
            task.setReturnValue(true);
            return null;
        } else {
            debug("%s ignoring task %s", ioboard.toString(), task.toString());
            task.setReturnValue(false);
            return null;
        }
        task.setReturnValue(ret);
        String err = ioboard.sendCmd('S');
        if (err != null) {
            if (pendingSensorStatusTask != null) {
                pendingSensorStatusTask.setReturnValue(false);
            }
            return new IoboardError(ioboard, err);
        }
        return this;
    }

// send the first message
    @Override
    public DeviceStateInterface init() {
        retries = 0;
        String err = ioboard.sendCmd('S');
        if (err != null) {
            if (pendingSensorStatusTask != null) {
                pendingSensorStatusTask.setReturnValue(false);
            }
            return new IoboardError(ioboard, err);
        }
        return null;
    }

    @Override
    public String toString() {
        return "IoboardStateMain";
    }

}
