package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;
import play.Logger;

@Entity
@Table( name = "lg_bag", schema = "public" )
public class LgBag extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bag_id", unique = true, nullable = false )
    @GeneratedValue
    public int bagId;
    @Column( name = "bag_code", nullable = false, length = 128 )
    public String bagCode;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @Temporal( TemporalType.DATE )
    @Column( name = "withdraw_date", length = 13 )
    public Date withdrawDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "bag" )
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>( 0 );
    
    
    public static LgBag GetCurrentBag() {
        LgBag currentBag = LgBag.find("order by creationDate desc").first();
        if ( currentBag.withdrawDate.before( new Date() ) ) {
            Logger.error("XXX Last bag found is withdraw!!!");
            return null;
        }
        return currentBag;
    }
    
    public String toString() {
        return "Bag: " + bagCode; // + " from: " + creationDate.toString() + " to: " + withdrawDate.toString();
    }
}
