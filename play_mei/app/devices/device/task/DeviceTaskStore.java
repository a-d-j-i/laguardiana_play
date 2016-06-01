package devices.device.task;

/**
 *
 * @author adji
 */
public class DeviceTaskStore extends DeviceTaskAbstract {
    
    private final int sequenceNumber;
    
    public DeviceTaskStore(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    @Override
    public String toString() {
        return "DeviceTaskStore : " + Integer.toString(sequenceNumber);
    }
    
}
