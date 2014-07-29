package devices.serial;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public interface SerialPortMessageParserInterface {

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException;
}
