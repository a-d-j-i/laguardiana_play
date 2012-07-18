package models.db;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_user_property", schema = "public" )
public class LgUserProperty extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "user_property_id", unique = true, nullable = false )
    @GeneratedValue
    public int lovId;
    @Column( name = "user_id", nullable = false )
    public int userId;
    @Column( name = "property", nullable = false, length = 64 )
    public String property;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "user_id", nullable = false, insertable = false, updatable = false )
    public LgUser user;
    @Column( name = "value", nullable = false, length = 128 )
    public String value;
}
