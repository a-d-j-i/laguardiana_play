package devices.mei.status;

import devices.device.DeviceStatusInterface;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;

/**
 *
 * @author adji
 */
public class MeiEbdsCurrentStatus implements DeviceStatusInterface {

    
    
    @Override
    public MeiEbdsStatusType getType() {
        return MeiEbdsStatusType.CURRENT_STATUS;
    }


}
