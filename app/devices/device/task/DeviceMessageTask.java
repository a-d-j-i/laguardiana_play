package devices.device.task;

import devices.device.DeviceMessageInterface;

/**
 *
 * @author adji
 */
public class DeviceMessageTask extends DeviceTaskAbstract {

    private final DeviceMessageInterface message;

    public DeviceMessageTask(Enum type, DeviceMessageInterface message) {
        super(type);
        this.message = message;
    }

    public DeviceMessageInterface getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DeviceMessageTask{" + "message=" + message + '}';
    }

}
