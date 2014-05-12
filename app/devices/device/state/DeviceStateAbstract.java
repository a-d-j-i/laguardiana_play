package devices.device.state;

import devices.device.DeviceAbstract.DeviceStateApi;
import devices.device.operation.DeviceOperationInterface;
import devices.device.response.DeviceResponseInterface;

/**
 *
 * @author adji
 */
abstract public class DeviceStateAbstract implements DeviceStateInterface {

    private final DeviceStateApi api;

    public DeviceStateAbstract(DeviceStateApi api) {
        this.api = api;
    }

    public DeviceStateApi getApi() {
        return api;
    }

    public DeviceStateAbstract init() {
        return this;
    }

    abstract public DeviceStateInterface step();

    // Executed by the inner thread, return null if not ready
    public DeviceResponseInterface sendDeviceOperation(DeviceOperationInterface operation, boolean debug) {
        return null;
    }

};
