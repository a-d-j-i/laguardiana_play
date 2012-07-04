package models;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_lov", schema = "public" )
public class LgLov extends GenericModel implements java.io.Serializable {

    @EmbeddedId
    @AttributeOverrides( {
        @AttributeOverride( name = "type", column =
        @Column( name = "type", nullable = false, length = 32 ) ),
        @AttributeOverride( name = "numericId", column =
        @Column( name = "numeric_id", nullable = false ) ),
        @AttributeOverride( name = "textId", column =
        @Column( name = "text_id", nullable = false, length = 32 ) ) } )
    public LgLovId id;
    @Column( name = "description", nullable = false, length = 256 )
    public String description;
    @Temporal( TemporalType.DATE )
    @Column( name = "end_date", nullable = false, length = 13 )
    public Date endDate;
}
