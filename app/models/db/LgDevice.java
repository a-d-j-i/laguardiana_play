/*
 I'm assuming that each machine has only one way of taking envelopes, so there is no device <-> envelope relation
 */
package models.db;

import devices.device.DeviceAbstract;
import devices.glory.GloryDE50Device;
import devices.mei.MeiEbdsDevice;
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
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_device", schema = "public")
public class LgDevice extends GenericModel implements java.io.Serializable {

    public enum DeviceType {

        OS_PRINTER() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return null;
                    }
                },
        IO_BOARD_V4520_1_0() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return null;
                    }
                },
        IO_BOARD_V4520_1_2() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return null;
                    }
                },
        IO_BOARD_MX220_1_0() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return null;
                    }
                },
        GLORY_DE50() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return new GloryDE50Device(machineDeviceId, this);
                    }
                },
        MEI_EBDS() {
                    @Override
                    public DeviceAbstract createDevice(Enum machineDeviceId) {
                        return new MeiEbdsDevice(machineDeviceId, this);
                    }
                };

        public enum DeviceClass {

            DEVICE_CLASS_PRINTER,
            DEVICE_CLASS_COUNTER,
            DEVICE_CLASS_IOBOARD,
        };

        abstract public DeviceAbstract createDevice(Enum machineDeviceId);

        @Override
        public String toString() {
            return name();
        }

    };
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
    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    public DeviceType deviceType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "device")
    public Set<LgDeviceProperty> deviceProperties = new HashSet<LgDeviceProperty>(0);

    // This is used only for those that implement CounterDevice
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "device")
    public Set<LgBatch> batches = new HashSet<LgBatch>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "device")
    public Set<LgDeviceSlot> slots = new HashSet<LgDeviceSlot>(0);

    // This is used only for those that implement IoBoardDevice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_id", nullable = true)
    public LgBag currentBag;

    public LgDevice(Enum machineDeviceId, DeviceType deviceType) {
        this.deviceType = deviceType;
        this.machineDeviceId = machineDeviceId.name();
        creationDate = new Date();
    }

    public static LgDevice getOrCreateByMachineId(Enum machineDeviceId, DeviceType deviceType) {
        LgDevice ret = find("select d from LgDevice d where machineDeviceId = ?", machineDeviceId.name()).first();
        if (ret != null) {
            Logger.debug("Found a device : %s %s", ret.machineDeviceId, ret.deviceType.name());
            return ret;
        }
        ret = new LgDevice(machineDeviceId, deviceType);
        ret.save();
        Logger.debug("Created device %d: %s %s", ret.deviceId, ret.machineDeviceId, ret.deviceType.name());
        return ret;
    }

    @Override
    public String toString() {
        return "deviceId=" + deviceId + ", machineDeviceId=" + machineDeviceId + ", creationDate=" + creationDate + ", endDate=" + endDate + ", currentBag=" + currentBag;
    }

}
