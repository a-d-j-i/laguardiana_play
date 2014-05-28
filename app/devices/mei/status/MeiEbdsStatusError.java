package devices.mei.status;

import devices.device.status.*;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;

/**
 *
 * @author adji
 */
public class MeiEbdsStatusError extends DeviceStatusError {

    public MeiEbdsStatusError(String error) {
        super(error);
    }

    public MeiEbdsStatusType getType() {
        return MeiEbdsStatusType.ERROR;
    }
}
