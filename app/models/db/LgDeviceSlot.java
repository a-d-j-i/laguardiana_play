package models.db;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_device_slot", schema = "public")
public class LgDeviceSlot extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "device_slot_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgDeviceSlotGenerator")
    @SequenceGenerator(name = "LgDeviceSlotGenerator", sequenceName = "lg_device_slot_sequence")
    public Integer deviceSlotId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    public LgDevice device;
    @Column(name = "slot", nullable = false)
    public String slot;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_type_id", nullable = false)
    public LgBillType billType;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;

    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    @Override
    public String toString() {
        return "LgDeviceSlot{" + "deviceSlotId=" + deviceSlotId + ", device=" + device + ", slot=" + slot + ", creationDate=" + creationDate + ", endDate=" + endDate + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.deviceSlotId != null ? this.deviceSlotId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LgDeviceSlot other = (LgDeviceSlot) obj;
        if (this.deviceSlotId != other.deviceSlotId && (this.deviceSlotId == null || !this.deviceSlotId.equals(other.deviceSlotId))) {
            return false;
        }
        return true;
    }

}
