package devices.mei.state;

import devices.device.state.*;
import devices.mei.MeiEbdsDevice;

/**
 *
 * @author adji
 */
abstract public class MeiEbdsStateAbstract extends DeviceStateAbstract implements DeviceStateInterface {

    final protected MeiEbdsDevice mei;

    public MeiEbdsStateAbstract(MeiEbdsDevice mei) {
        this.mei = mei;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateAbstract";
    }

}
