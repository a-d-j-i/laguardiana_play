package models.db;

import javax.persistence.*;
import models.BillValue;
import models.lov.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_bill", schema = "public")
//        uniqueConstraints = @UniqueConstraint(columnNames = {"deposit_id", "batch_id", "device_id", "bill_type_id"})
//)
public class LgBill extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "bill_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgBillGenerator")
    @SequenceGenerator(name = "LgBillGenerator", sequenceName = "lg_bill_sequence")
    public Integer billId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", nullable = false)
    public LgDeposit deposit;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    public LgBatch batch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_type_id", nullable = false)
    public LgBillType billType;
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    public LgBill(int quantity, LgBillType billType) {
        this.quantity = quantity;
        this.billType = billType;
    }

    @Override
    public String toString() {
        Integer q = quantity;
        Integer d = billType.denomination;
        Integer t = quantity * billType.denomination;
        return (q.toString() + " *  $" + d.toString() + " = " + t.toString()
                + "(" + Currency.findByNumericId(billType.unitLov).toString() + ")");
    }

    public Integer getTotal() {
        return quantity * billType.denomination;
    }

    public BillValue getValue() {
        return billType.getValue();
    }
}
