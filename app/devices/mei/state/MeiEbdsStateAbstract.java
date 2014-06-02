package devices.mei.state;

import devices.device.state.*;
import devices.mei.MeiEbds;

/**
 *
 * @author adji
 */
abstract public class MeiEbdsStateAbstract extends DeviceStateAbstract implements DeviceStateInterface {

    final protected MeiEbds mei;

    public MeiEbdsStateAbstract(MeiEbds mei) {
        this.mei = mei;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateAbstract";
    }

}
