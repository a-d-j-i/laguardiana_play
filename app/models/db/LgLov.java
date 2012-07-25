package models.db;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_lov", schema = "public" )
@DiscriminatorColumn( name = "type", length=32 )
abstract public class LgLov extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "lov_id", unique = true, nullable = false )
    @GeneratedValue
    public int lovId;
    //@Column( name = "type", nullable = false, length = 32 )
    //public String type;
    @Column( name = "numeric_id", nullable = false )
    public int numericId;
    @Column( name = "text_id", nullable = false, length = 32 )
    public String textId;
    @Column( name = "description", nullable = false, length = 256 )
    public String description;
    @Temporal( TemporalType.DATE )
    @Column( name = "end_date", nullable = false, length = 13 )
    public Date endDate;

    static public LgLov findByNumericId( Integer numericId ) {
        return LgLov.find( "select l from LgLov l where numericId = ?", numericId ).first();
    }

    static public LgLov findByTextId( String textId ) {
        return LgLov.find( "select l from LgLov l where textId = ?", textId ).first();
    }
}
