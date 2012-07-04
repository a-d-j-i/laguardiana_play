package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_deposit", schema = "public" )
public class LgDeposit extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "deposit_id", unique = true, nullable = false )
    @GeneratedValue
    public int depositId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "user_id", nullable = false )
    public LgUser lgUser;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "bag_id", nullable = false )
    public LgBag lgBag;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @Temporal( TemporalType.DATE )
    @Column( name = "start_date", length = 13 )
    public Date startDate;
    @Temporal( TemporalType.DATE )
    @Column( name = "finish_date", length = 13 )
    public Date finishDate;
    @Column( name = "user_code", length = 128 )
    public String userCode;
    @Column( name = "user_code_lov" )
    public Integer userCodeLov;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgDeposit" )
    public Set<LgEvent> lgEvents = new HashSet<LgEvent>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgDeposit" )
    public Set<LgEnvelope> lgEnvelopes = new HashSet<LgEnvelope>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgDeposit" )
    public Set<LgBill> lgBills = new HashSet<LgBill>( 0 );
}
