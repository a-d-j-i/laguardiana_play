package models;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import play.db.jpa.GenericModel;

@Embeddable
public class LgUserPropertyId extends GenericModel implements java.io.Serializable {

    @Column( name = "user_id", nullable = false )
    public int userId;
    @Column( name = "property", nullable = false, length = 64 )
    public String property;
}
