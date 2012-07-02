package models;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import play.db.jpa.GenericModel;

@Embeddable
public class LgLovId extends GenericModel implements java.io.Serializable {

    @Column( name = "type", nullable = false, length = 32 )
    String type;
    @Column( name = "numeric_id", nullable = false )
    int numericId;
    @Column( name = "text_id", nullable = false, length = 32 )
    String textId;
}
