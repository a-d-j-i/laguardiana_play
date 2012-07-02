package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_user_property", schema = "public" )
public class LgUserProperty extends GenericModel implements java.io.Serializable {

    @EmbeddedId
    @AttributeOverrides( {
        @AttributeOverride( name = "userId", column =
        @Column( name = "user_id", nullable = false ) ),
        @AttributeOverride( name = "property", column =
        @Column( name = "property", nullable = false, length = 64 ) ) } )
    LgUserPropertyId id;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "user_id", nullable = false, insertable = false, updatable = false )
    LgUser lgUser;
    @Column( name = "value", nullable = false, length = 128 )
    String value;
}
