package models.db;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_system_property", schema = "public")
public class LgSystemProperty extends GenericModel implements java.io.Serializable {

    public enum Types {

        DEFAULT_CURRENCY,
        MACHINE_CODE,
        MACHINE_DESCRIPTION,
        CLIENT_CODE,
        CLIENT_DESCRIPTION,
        PROVIDER_DESCRIPTION,
        BRANCH_CODE,
        MAX_BILLS_PER_BAG,
        ENVELOPE_BILL_EQUIVALENCY,
        WITHDRAW_USER;

        public String getTypeName() {
            return name().toLowerCase();
        }
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

    static public String getProperty(Types type) {
        return getProperty(type.getTypeName());
    }

    static private String getProperty(String name) {
        LgSystemProperty l = LgSystemProperty.find("select p from LgSystemProperty p where p.name = ?", name).first();
        if (l == null) {
            return null;
        }
        return l.value;
    }

    public static Boolean isProperty(String name) {
        String p = LgSystemProperty.getProperty(name);
        if (p != null && !p.isEmpty()) {
            if (p.equalsIgnoreCase("false") || p.equalsIgnoreCase("off")) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "LgSystemProperty{" + "propertyId=" + propertyId + ", name=" + name + ", value=" + value + '}';
    }
}
