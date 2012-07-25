package models.db;

import java.util.List;
import java.util.Arrays;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bill", schema = "public" )
public class LgBill extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bill_id", unique = true, nullable = false )
    @GeneratedValue
    public int billId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id", nullable = false )
    public LgDeposit deposit;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "batch_id", nullable = false )
    public LgBatch batch;
    @Column( name = "slot_id", nullable = false )
    public int slotId;
    @Column( name = "quantity", nullable = false )
    public int quantity;
    @Column( name = "denomination", nullable = false )
    public int denomination;
    @Column( name = "unit_lob", nullable = false )
    public int unitLob;
    
    public LgBill(LgBatch batch, int slotId, int quantity, int denomination, int unitLob, LgDeposit deposit) {
        this.batch = batch;
        this.slotId = slotId;
        this.quantity = quantity;
        this.denomination = denomination;
        this.unitLob = unitLob;
        this.deposit = deposit;
        batch.addBill(this);
    }

    public void addToDeposit(LgDeposit deposit) {
        this.deposit = deposit;
    }
    
    public String toString() {
        Integer q = quantity;
        Integer d = denomination;
        Integer t = quantity * denomination; 
        return q.toString() + " *  $" + d.toString() + " = " + t.toString() + "("+ LgLov.FromBillCode(unitLob).toString() +")";
    }

    public int getTotal() {
        return quantity * denomination;
    }    
    
    @Override
    public boolean equals( Object obj ) {
        // We need to check this
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final LgBill other = ( LgBill ) obj;
        boolean equal;
        
        equal = this.quantity == other.quantity;
        equal = equal && (this.denomination == other.denomination);
        equal = equal && (this.unitLob == other.unitLob);
        equal = equal && (this.slotId == other.slotId);
        equal = equal && (this.deposit == other.deposit);
        equal = equal && (this.batch == other.batch);
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = hash * batch.hashCode();
        hash = hash * slotId;
        hash = hash * quantity;
        hash = hash * denomination;
        return hash;
    }
}
