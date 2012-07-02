package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bag", schema = "public" )
public class LgBag extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bag_id", unique = true, nullable = false )
    int bagId;
    @Column( name = "bag_code", nullable = false, length = 128 )
    String bagCode;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    Date creationDate;
    @Temporal( TemporalType.DATE )
    @Column( name = "withdraw_date", length = 13 )
    Date withdrawDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgBag" )
    Set<LgDeposit> lgDeposits = new HashSet<LgDeposit>( 0 );
}
