package devices.device.task;

import devices.device.operation.DeviceOperationInterface;
import devices.device.response.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;

/**
 *
 * @author adji
 */
public class DeviceTaskOperation extends DeviceTaskAbstract<DeviceResponseInterface> {

    final DeviceOperationInterface operation;
    final boolean debug;

    public DeviceTaskOperation(DeviceOperationInterface operation, boolean debug) {
        this.operation = operation;
        this.debug = debug;
    }

    @Override
    protected DeviceStateInterface call(DeviceStateInterface currentState) {
        setReturnValue(currentState.sendDeviceOperation(operation, debug));
        return null;
    }
}
