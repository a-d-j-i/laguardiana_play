package models.db;

import java.security.SecureRandom;
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
        WITHDRAW_USER,
        CRAPAUTH_VARIABLE_ID,
        CRAPAUTH_CONSTANT_ID;

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
        LgSystemProperty l = getProperty(type.getTypeName());
        if (l == null) {
            return null;
        }
        return l.value;
    }

    static private LgSystemProperty getProperty(String name) {
        LgSystemProperty l = LgSystemProperty.find("select p from LgSystemProperty p where p.name = ?", name).first();
        return l;
    }

    public static Boolean isProperty(String name) {
        LgSystemProperty p = LgSystemProperty.getProperty(name);
        if (p != null && !p.value.isEmpty()) {
            if (p.value.equalsIgnoreCase("false") || p.value.equalsIgnoreCase("off")) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static void initCrapId() {
        LgSystemProperty crapId = getProperty(Types.CRAPAUTH_VARIABLE_ID.getTypeName());
        if (crapId == null) {
            crapId = new LgSystemProperty();
            crapId.name = Types.CRAPAUTH_VARIABLE_ID.getTypeName();
        }
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[3];
        random.nextBytes(bytes);
        int val = 0;
        for (int i = 0; i < 3; i++) {
            val = val * 100;
            val += ((((int) bytes[i] & 0x0F) % 9) + 1) * 10 + (((((int) bytes[i] & 0xF0) >> 4) % 9) + 1);
        }
        crapId.value = Integer.toString(val);
        crapId.save();
    }

    @Override
    public String toString() {
        return "LgSystemProperty{" + "propertyId=" + propertyId + ", name=" + name + ", value=" + value + '}';
    }
}
