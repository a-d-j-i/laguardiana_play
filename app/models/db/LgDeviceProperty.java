package models.db;

import java.util.List;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_device_property", schema = "public")
public class LgDeviceProperty extends GenericModel implements java.io.Serializable {

    public enum EditType {

        NOT_EDITABLE,
        STRING,
        BOOLEAN,
        INTEGER,
        PRINTER_LIST;
    }

    @Id
    @Column(name = "device_property_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgDevicePropertyGenerator")
    @SequenceGenerator(name = "LgDevicePropertyGenerator", sequenceName = "lg_device_property_sequence")
    public Integer devicePropertyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    public LgDevice device;
    @Column(name = "name", nullable = false, length = 64)
    public String name;
    @Column(name = "value", nullable = true, length = 128)
    public String value;
    @Column(name = "edit_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    public LgDeviceProperty.EditType editType = LgDeviceProperty.EditType.NOT_EDITABLE;

    static public LgDeviceProperty setOrCreateProperty(LgDevice device, String property, String value) {
        LgDeviceProperty l = getProperty(device, property);
        if (l == null) {
            l = new LgDeviceProperty();
            l.device = device;
            l.name = property;
        }
        l.value = value;
        l.save();
        return l;
    }

    public static LgDeviceProperty getOrCreateProperty(LgDevice device, String property, LgDeviceProperty.EditType editType) {
        LgDeviceProperty l = getProperty(device, property);
        if (l != null) {
            Logger.debug("GetOrCreate device %d, %s property %s", l.device.deviceId, l.device.machineDeviceId, l.name);
            return l;
        }
        l = new LgDeviceProperty();
        l.name = property;
        l.device = device;
        l.editType = editType;
        l.value = "";
        device.deviceProperties.add(l);
        l.save();
        Logger.debug("Creating device %d, %s property %s", l.device.deviceId, l.device.machineDeviceId, l.name);
        return l;
    }

    static public LgDeviceProperty getProperty(LgDevice device, String name) {
        LgDeviceProperty l = LgDeviceProperty.find("select p from LgDeviceProperty p where device = ? and trim( p.name ) = trim( ? )", device, name).first();
        return l;
    }

    public static List<LgDeviceProperty> getEditables(LgDevice device) {
        return LgDeviceProperty.find("select p from LgDeviceProperty p where device = ? and edit_type != ? order by p.name",
                device,
                LgDeviceProperty.EditType.NOT_EDITABLE.ordinal()).fetch();
    }

    @Override
    public String toString() {
        return "LgDeviceProperty{" + "device=" + device + ", name=" + name + ", value=" + value + ", editType=" + editType + '}';
    }

}
