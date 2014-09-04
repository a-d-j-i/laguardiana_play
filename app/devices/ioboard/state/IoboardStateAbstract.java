package devices.ioboard.state;

import devices.device.state.*;
import devices.ioboard.IoboardDevice;

/**
 *
 * @author adji
 */
abstract public class IoboardStateAbstract extends DeviceStateAbstract implements DeviceStateInterface {

    final protected IoboardDevice ioboard;

    public IoboardStateAbstract(IoboardDevice ioboard) {
        this.ioboard = ioboard;
    }
    
    @Override
    public String toString() {
        return "IoboardStateAbstract";
    }

}
