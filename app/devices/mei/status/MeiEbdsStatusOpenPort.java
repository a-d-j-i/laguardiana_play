package devices.mei.status;

import devices.device.status.*;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;

/**
 *
 * @author adji
 */
public class MeiEbdsStatusOpenPort extends DeviceStatusOpenPort {

    public MeiEbdsStatusOpenPort(String port) {
        super(port);
    }

    public MeiEbdsStatusType getType() {
        return MeiEbdsStatusType.OPEN_PORT;
    }
}
