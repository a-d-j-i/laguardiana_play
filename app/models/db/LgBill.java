package models.db;

import models.lov.*;

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
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "bill_type_id", nullable = false )
    public LgBillType billType;

    public LgBill( LgBatch batch, int slotId, int quantity, LgBillType billType, LgDeposit deposit ) {
        this.batch = batch;
        this.slotId = slotId;
        this.quantity = quantity;
        this.deposit = deposit;
        this.billType = billType;
        batch.addBill( this );
    }

    public void addToDeposit( LgDeposit deposit ) {
        this.deposit = deposit;
    }

    @Override
    public String toString() {
        Integer q = quantity;
        Integer d = billType.denomination;
        Integer t = quantity * billType.denomination;
        return ( q.toString() + " *  $" + d.toString() + " = " + t.toString()
                + "(" + MoneyUnit.findByNumericId( billType.unitLov ).toString() + ")" );
    }

    public int getTotal() {
        return quantity * billType.denomination;
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
        equal = equal && ( this.billType.denomination == other.billType.denomination );
        equal = equal && ( this.billType.unitLov == other.billType.unitLov );
        equal = equal && ( this.slotId == other.slotId );
        equal = equal && ( this.deposit == other.deposit );
        equal = equal && ( this.batch == other.batch );
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = hash * batch.hashCode();
        hash = hash * slotId;
        hash = hash * quantity;
        hash = hash * billType.denomination;
        return hash;
    }
}
