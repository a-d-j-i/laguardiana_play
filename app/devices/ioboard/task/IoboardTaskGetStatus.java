package devices.ioboard.task;

import devices.device.task.DeviceTaskAbstract;
import devices.ioboard.status.IoboardStatus;

/**
 *
 * @author adji
 */
public class IoboardTaskGetStatus extends DeviceTaskAbstract {

    IoboardStatus status = null;

    @Override
    public String toString() {
        return "IoboardTaskGetStatus{" + '}';
    }

    public void setResponse(IoboardStatus status) {
        this.status = status;
        setReturnValue(true);
    }

    public IoboardStatus getSensorStatus() {
        return status;
    }

}
