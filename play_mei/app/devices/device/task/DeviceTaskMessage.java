package devices.device.task;

import devices.device.DeviceMessageInterface;
import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class DeviceTaskMessage extends DeviceTaskAbstract {

    private final DeviceMessageInterface message;
    private final DeviceResponseInterface response;

    public DeviceTaskMessage(DeviceMessageInterface msg, DeviceResponseInterface response) {
        this.message = msg;
        this.response = response;
    }

    public DeviceMessageInterface getMessage() {
        return message;
    }

    public DeviceResponseInterface getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "DeviceTaskMessage{" + "message=" + message + ", response=" + response + '}';
    }

}