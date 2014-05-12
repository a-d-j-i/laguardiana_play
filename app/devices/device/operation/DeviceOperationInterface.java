package devices.device.operation;

import devices.device.response.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public interface DeviceOperationInterface {

    public byte[] getCmdStr();

    public DeviceResponseInterface getResponse(byte[] data);

}
