package devices.ioboard.task;

import devices.device.task.DeviceTaskAbstract;
import devices.ioboard.response.IoboardStatusResponse;

/**
 *
 * @author adji
 */
public class IoboardTaskGetSensorStatus extends DeviceTaskAbstract {

    IoboardStatusResponse sensorStatus = null;

    @Override
    public String toString() {
        return "IoboardTaskGetSensorStatus{" + '}';
    }

    public void setResponse(IoboardStatusResponse sensorStatus) {
        this.sensorStatus = sensorStatus;
        setReturnValue(true);
    }

    public IoboardStatusResponse getSensorStatus() {
        return sensorStatus;
    }

}
