package models;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_lov", schema = "public" )
public class LgLov extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "lov_id", unique = true, nullable = false )
    @GeneratedValue
    public int lovId;
    @Column( name = "type", nullable = false, length = 32 )
    public String type;
    @Column( name = "numeric_id", nullable = false )
    public int numericId;
    @Column( name = "text_id", nullable = false, length = 32 )
    public String textId;
    @Column( name = "description", nullable = false, length = 256 )
    public String description;
    @Temporal( TemporalType.DATE )
    @Column( name = "end_date", nullable = false, length = 13 )
    public Date endDate;
}
