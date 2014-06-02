package devices.serial;

import devices.device.DeviceMessageInterface;

/**
 *
 * @author adji
 */
public interface SerialPortMessageParserInterface {

    public DeviceMessageInterface getMessage(SerialPortAdapterInterface serialPort) throws InterruptedException;
}
