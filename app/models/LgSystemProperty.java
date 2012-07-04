package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_system_property", schema = "public" )
public class LgSystemProperty extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "property_id", unique = true, nullable = false )
    @GeneratedValue
    public int propertyId;
    @Column( name = "name", nullable = false, length = 64 )
    public String name;
    @Column( name = "value", nullable = false, length = 128 )
    public String value;
}
