package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;

/**
 *
 * @author adji
 */
public class GloryDE50TaskStoreDeposit extends DeviceTaskAbstract {

    final Integer sequenceNumber;

    public GloryDE50TaskStoreDeposit(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

}