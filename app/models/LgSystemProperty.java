package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_system_property", schema = "public" )
public class LgSystemProperty extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "property_id", unique = true, nullable = false )
    int propertyId;
    @Column( name = "name", nullable = false, length = 64 )
    String name;
    @Column( name = "value", nullable = false, length = 128 )
    String value;
}
