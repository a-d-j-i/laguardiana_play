package models;

import java.util.List;
import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

import play.Logger;

@Entity
@Table( name = "lg_lov", schema = "public" )
public class LgLov extends GenericModel implements java.io.Serializable {

    public static final String UserCodeReference = "UserCodeReference"; // used for user reference codes in deposits

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
    
    public static List<LgLov> getReferenceCodes() {
        return LgLov.find("byType", "UserCodeReference").fetch();
    }
    
    
    public static LgLov FromUserCodeReference(String userCodeReference) {
        Logger.error("searching for: %s", userCodeReference);
        return LgLov.find("byTypeAndTextId", UserCodeReference, userCodeReference).first();
    }
    
    public static LgLov FromUserCodeReference(int userCodeReference) {
        Logger.error("2searching for: %d", userCodeReference);
        LgLov l = LgLov.find("byTypeAndNumericId", UserCodeReference, userCodeReference).first();
        Logger.error("ref: %s", l.type); 
        return l;
    }
        
    public String toString() {
        return textId;
    }
}
