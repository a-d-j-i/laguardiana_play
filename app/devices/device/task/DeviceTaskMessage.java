package devices.device.task;

import devices.device.DeviceMessageInterface;
import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class DeviceTaskMessage extends DeviceTaskAbstract {

    private final DeviceMessageInterface message;
    private final DeviceTaskResponse response;

    public DeviceTaskMessage(DeviceMessageInterface msg, DeviceTaskResponse response) {
        this.message = msg;
        this.response = response;
    }

    public DeviceMessageInterface getMessage() {
        return message;
    }

    public DeviceResponseInterface getResponse() {
        return response.getResponse();
    }

    @Override
    public void setReturnValue(boolean returnValue) {
        response.setReturnValue(returnValue);
        super.setReturnValue(returnValue);
    }

    @Override
    public String toString() {
        return "DeviceTaskMessage{" + "message=" + message + ", response=" + response + '}';
    }

}
