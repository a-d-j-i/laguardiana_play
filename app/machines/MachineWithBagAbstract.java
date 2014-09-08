package machines;

import devices.device.status.DeviceStatusInterface;
import devices.ioboard.response.IoboardStateResponse;
import devices.ioboard.status.IoboardStatus;
import devices.ioboard.task.IoboardTaskGetStatus;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import play.Logger;

/**
 * Composite of devices.
 *
 * @author adji
 */
abstract public class MachineWithBagAbstract extends MachineAbstract implements MachineInterface {

    private final MachineDeviceDecorator ioboard;

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
}
