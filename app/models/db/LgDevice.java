package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import machines.Machine.DeviceType;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_device", schema = "public")
public class LgDevice extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "device_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgDeviceGenerator")
    @SequenceGenerator(name = "LgDeviceGenerator", sequenceName = "lg_device_sequence")
    public Integer deviceId;

    @Column(name = "machine_device_id", unique = true, nullable = false)
    public String machineDeviceId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;
    @Column(name = "device_type", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    public DeviceType deviceType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "device")
    public Set<LgDeviceProperty> deviceProperties = new HashSet<LgDeviceProperty>(0);

    // This is used only for those that implement CounterDevice
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "device")
    public Set<LgBatch> batches = new HashSet<LgBatch>(0);

    // This is used only for those that implement IoBoardDevice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_id", nullable = true)
    public LgBag currentBag;

    public LgDevice(DeviceType deviceType, String machineDeviceId) {
        this.deviceType = deviceType;
        this.machineDeviceId = machineDeviceId;
        creationDate = new Date();
    }

    public static LgDevice getOrCreateByMachineId(DeviceType deviceType, String machineDeviceId) {
        LgDevice ret = find("select d from LgDevice d where machineDeviceId = ?", machineDeviceId).first();
        if (ret != null) {
            return ret;
        }
        ret = new LgDevice(deviceType, machineDeviceId);
        ret.save();
        return ret;
    }

    @Override
    public String toString() {
        return "deviceId=" + deviceId + ", machineDeviceId=" + machineDeviceId + ", creationDate=" + creationDate + ", endDate=" + endDate + ", currentBag=" + currentBag;
    }

}
