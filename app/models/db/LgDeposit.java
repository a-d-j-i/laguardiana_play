package models.db;

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
    public LgUser user;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "bag_id", nullable = false )
    public LgBag bag;
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
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit" )
    public Set<LgEvent> events = new HashSet<LgEvent>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit" )
    public Set<LgEnvelope> envelopes = new HashSet<LgEnvelope>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit" )
    public Set<LgBill> bills = new HashSet<LgBill>( 0 );
    
    public void addBill(LgBill bill) {
        bill.addToDeposit(this);
        this.bills.add(bill);
    }
    
    public void addBatch(LgBatch batch) {
        for (LgBill bill: batch.bills) {
            this.addBill(bill);
            //bill.save();
        }
    }
    
    public LgDeposit( LgUser user, String userCode, LgLov userCodeLov) {
        this.bag = LgBag.GetCurrentBag();
        this.user = user;
        this.userCode = userCode;
        this.userCodeLov = userCodeLov.numericId;   
        this.creationDate = new Date();
    }
    
    public LgLov getUserCodeLov() {
        return LgLov.FromUserCodeReference(userCodeLov);
    }
    
    @Override
    public String toString() {
        LgLov uc = this.getUserCodeLov();
        return "Deposit by: "+user.toString()+" in: "+bag.toString()+" codes:["+userCode+"/"+uc.toString()+"]";
    }
}
