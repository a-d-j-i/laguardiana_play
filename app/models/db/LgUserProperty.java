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

    @Override
    public String toString() {
        return "LgUserProperty{" + "userPropertyId=" + userPropertyId + ", property=" + property + ", user=" + user.userId + ", value=" + value + '}';
    }
}
