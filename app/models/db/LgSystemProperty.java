package models.db;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_system_property", schema = "public")
public class LgSystemProperty extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "property_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer propertyId;
    @Column(name = "name", nullable = false, length = 64)
    public String name;
    @Column(name = "value", nullable = true, length = 128)
    public String value;

    static public String getProperty(String name) {
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
