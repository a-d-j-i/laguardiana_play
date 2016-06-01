package machines;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskReset;
import devices.ioboard.response.IoboardStateResponse;
import devices.ioboard.status.IoboardStatus;
import static devices.ioboard.status.IoboardStatus.IoboardBagApprovedState.BAG_APROVED;
import static devices.ioboard.status.IoboardStatus.IoboardBagApprovedState.BAG_APROVE_CONFIRM;
import static devices.ioboard.status.IoboardStatus.IoboardBagApprovedState.BAG_APROVE_WAIT;
import static devices.ioboard.status.IoboardStatus.IoboardBagApprovedState.BAG_NOT_APROVED;
import devices.ioboard.task.IoboardTaskAproveBag;
import devices.ioboard.task.IoboardTaskConfirmBag;
import devices.ioboard.task.IoboardTaskGetStatus;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import models.Configuration;
import models.db.LgBag;
import play.Logger;

/**
 * Composite of devices.
 *
 * @author adji
 */
abstract public class MachineWithBagAbstract extends MachineAbstract implements MachineInterface {

    protected final MachineDeviceDecorator ioboard;

    protected MachineWithBagAbstract(MachineDeviceDecorator ioboard) {
        this.ioboard = ioboard;
        addDevice(ioboard);
    }

    public boolean isBagReady() {
        IoboardTaskGetStatus deviceTask = new IoboardTaskGetStatus();
        try {
            ioboard.submit(deviceTask).get(1000, TimeUnit.MILLISECONDS);
            IoboardStatus st = deviceTask.getSensorStatus();
            return (st != null && st.getBagState() == IoboardStateResponse.BAG_STATE.BAG_STATE_INPLACE);
        } catch (InterruptedException ex) {
            Logger.error("Exception trying to get ioboard status " + ex.toString());
        } catch (ExecutionException ex) {
            Logger.error("Exception trying to get ioboard status " + ex.toString());
        } catch (TimeoutException ex) {
            Logger.error("Exception trying to get ioboard status " + ex.toString());
        }
        return false;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface status) {
        if (dev == ioboard && status.is(IoboardStatus.class)) {
            onIoBoardEvent((IoboardStatus) status);
        }
        super.onDeviceEvent(dev, status);
    }

    @Override
    public boolean onReset() {
        ioboard.submitSynchronous(new DeviceTaskReset());
        return super.onReset();
    }

    private void onIoBoardEvent(IoboardStatus st) {
        if (Configuration.isIgnoreIoBoard()) {
            return;
        }
        if (st == null) {
            Logger.debug("onIoBoardEvent status is null");
            return;
        }
//        IoBoardEvent.save(status, status.toString());
//        Logger.debug("OnIoBoardEvent event %s", st.toString());
//            if (status.getBagState() != IoBoard.BAG_STATE.BAG_STATE_INPLACE || status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
        if (st.getBagState() != IoboardStateResponse.BAG_STATE.BAG_STATE_INPLACE) {
            // if bag not in place rotate current bag.
            LgBag.withdrawBag(true);
        }
        if (st.getBagState() == IoboardStateResponse.BAG_STATE.BAG_STATE_ERROR) {
            Logger.debug("onIoBoardEvent sending reset");
            ioboard.submitSynchronous(new DeviceTaskReset());
        }
        // Bag change.
        if (st.getBagState() == IoboardStateResponse.BAG_STATE.BAG_STATE_INPLACE) {
            switch (st.getBagApprovedState()) {
                case BAG_NOT_APROVED:
                    ioboard.submit(new IoboardTaskAproveBag());
                    /*if (!manager.collect()) {
                     modelError.setError(ModelError.ERROR_CODE.ERROR_TRYING_TO_COLLECT, "error trying to collect");
                     }*/
                    break;
                case BAG_APROVE_WAIT:
                    break;
                case BAG_APROVED:
                    // Bag aproved, recover from error.
                    break;
                case BAG_APROVE_CONFIRM:
                    LgBag.placeBag();
                    ioboard.submit(new IoboardTaskConfirmBag());
                    break;
            }
            /*if (u != null
             && status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED
             && !Configuration.isIgnoreBag()) {
             modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "Bag rotated during deposit");
             }*/
        }
    }

}
