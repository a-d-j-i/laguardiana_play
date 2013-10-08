package models;

/**
 *
 * @author adji
 */
public class ItemQuantity {

    public Long bills = new Long(0);
    public Long envelopes = new Long(0);

    @Override
    public String toString() {
        return "ItemQuantity{" + "bills=" + bills + ", envelopes=" + envelopes + '}';
    }
}