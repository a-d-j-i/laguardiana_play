package devices.device.task;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class DeviceTaskResponse extends DeviceTaskAbstract {

    private DeviceResponseInterface response;

    public DeviceTaskResponse(DeviceResponseInterface response) {
        this.response = response;
    }

    public DeviceResponseInterface getResponse() {
        return response;
    }

    public void setResponse(DeviceResponseInterface response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "DeviceTaskResponse{" + "response=" + response + '}';
    }

}
