package models.db;

import java.util.List;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_system_property", schema = "public")
public class LgSystemProperty extends GenericModel implements java.io.Serializable {

    public enum EditType {

        NOT_EDITABLE,
        STRING,
        BOOLEAN,
        INTEGER;

    }

    @Id
    @Column(name = "property_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgSystemPropertyGenerator")
    @SequenceGenerator(name = "LgSystemPropertyGenerator", sequenceName = "lg_system_property_sequence")
    public Integer propertyId;
    @Column(name = "name", nullable = false, length = 64)
    public String name;
    @Column(name = "value", nullable = true, length = 128)
    public String value;
    @Column(name = "edit_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    public EditType editType = EditType.NOT_EDITABLE;

    static public void setOrCreateProperty(String property, String value) {
        LgSystemProperty l = getProperty(property);
        if (l == null) {
            l = new LgSystemProperty();
            l.name = property;
        }
        l.value = value;
        l.save();
    }

    static public LgSystemProperty getProperty(String name) {
        LgSystemProperty l = LgSystemProperty.find("select p from LgSystemProperty p where trim( p.name ) = trim( ? )", name).first();
        return l;
    }

    public static List<LgSystemProperty> getEditables() {
        return LgSystemProperty.find("select p from LgSystemProperty p where edit_type != ? order by p.name", EditType.NOT_EDITABLE.ordinal()).fetch();
    }

    @Override
    public String toString() {
        return "LgSystemProperty{" + "propertyId=" + propertyId + ", name=" + name + ", value=" + value + '}';
    }
}
