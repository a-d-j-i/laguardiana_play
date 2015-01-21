package models.db;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_user_property", schema = "public")
public class LgUserProperty extends GenericModel implements java.io.Serializable {

    public enum Types {

        CRAP_AUTH;

        public String getTypeName() {
            return name().toLowerCase();
        }
    }
    @Id
    @Column(name = "user_property_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgUserPropertyGenerator")
    @SequenceGenerator(name = "LgUserPropertyGenerator", sequenceName = "lg_user_property_sequence")
    public Integer userPropertyId;
    @Column(name = "property", nullable = false, length = 64)
    public String property;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public LgUser user;
    @Column(name = "value", nullable = false, length = 128)
    public String value;

    static public String getProperty(LgUserProperty.Types type) {
        LgUserProperty l = getProperty(type.getTypeName());
        if (l == null) {
            return null;
        }
        return l.value;
    }

    static public Boolean isProperty(LgUserProperty.Types type) {
        return isProperty(type.getTypeName());
    }

    static private LgUserProperty getProperty(String name) {
        LgUserProperty l = LgUserProperty.find("select p from LgUserProperty p where trim( p.property ) = trim( ? )", name).first();
        return l;
    }

    static private Boolean isProperty(String name) {
        LgUserProperty p = LgUserProperty.getProperty(name);
        if (p != null && !p.value.isEmpty()) {
            if (p.value.equalsIgnoreCase("false") || p.value.equalsIgnoreCase("off")) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "LgUserProperty{" + "userPropertyId=" + userPropertyId + ", property=" + property + ", user=" + user.userId + ", value=" + value + '}';
    }
}
