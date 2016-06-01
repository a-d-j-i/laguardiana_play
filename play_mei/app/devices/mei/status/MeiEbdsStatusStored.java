package devices.mei.status;

/**
 *
 * @author adji
 */
public class MeiEbdsStatusStored extends MeiEbdsStatusReadyToStore {

    public MeiEbdsStatusStored(String slot) {
        super(slot);
    }

    @Override
    public String toString() {
        return "MeiEbdsStatusStored{" + "slot=" + slot + '}';
    }

}
