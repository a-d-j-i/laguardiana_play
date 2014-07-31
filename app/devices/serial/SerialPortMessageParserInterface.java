package devices.serial;

import devices.device.DeviceResponseInterface;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author adji
 */
public interface SerialPortMessageParserInterface {

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException, TimeoutException;
}
