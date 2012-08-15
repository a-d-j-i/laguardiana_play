package models.db;

import javax.persistence.*;
import models.lov.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bill", schema = "public")
public class LgBill extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bill_id", unique = true, nullable = false)
    @GeneratedValue
    public int billId;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "deposit_id", nullable = false)
    public LgDeposit deposit;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "batch_id", nullable = false)
    public LgBatch batch;
    @Column( name = "quantity", nullable = false)
    public int quantity;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "bill_type_id", nullable = false)
    public LgBillType billType;

    public LgBill(LgBatch batch, int quantity, LgBillType billType, LgDeposit deposit) {
        this.batch = batch;
        this.quantity = quantity;
        this.deposit = deposit;
        this.billType = billType;
        batch.addBill(this);
    }

    public void addToDeposit(LgDeposit deposit) {
        this.deposit = deposit;
    }

    @Override
    public String toString() {
        Integer q = quantity;
        Integer d = billType.denomination;
        Integer t = quantity * billType.denomination;
        return (q.toString() + " *  $" + d.toString() + " = " + t.toString()
                + "(" + MoneyUnit.findByNumericId(billType.unitLov).toString() + ")");
    }

    public int getTotal() {
        return quantity * billType.denomination;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.billId;
        hash = 89 * hash + (this.deposit != null ? this.deposit.hashCode() : 0);
        hash = 89 * hash + (this.batch != null ? this.batch.hashCode() : 0);
        hash = 89 * hash + this.quantity;
        hash = 89 * hash + (this.billType != null ? this.billType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LgBill other = (LgBill) obj;
        if (this.billId != other.billId) {
            return false;
        }
        if (this.deposit != other.deposit && (this.deposit == null || !this.deposit.equals(other.deposit))) {
            return false;
        }
        if (this.batch != other.batch && (this.batch == null || !this.batch.equals(other.batch))) {
            return false;
        }
        if (this.quantity != other.quantity) {
            return false;
        }
        if (this.billType != other.billType && (this.billType == null || !this.billType.equals(other.billType))) {
            return false;
        }
        return true;
    }
}
