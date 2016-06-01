package models.db;

import java.util.List;
import javax.persistence.*;
import models.BillValue;
import play.Logger;
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

    public LgBill(LgDeposit d, LgBatch batch, LgBillType billType) {
        this.deposit = d;
        this.batch = batch;
        this.billType = billType;
        this.quantity = 0;
    }

    static public LgBill getOrCreate(LgDeposit d, LgBatch batch, LgBillType billType) {
        List<LgBill> l = LgBill.find("select b from LgBill b where b.deposit = ? and b.batch = ? and b.billType = ? order by billId", d, batch, billType).fetch();
        if (l.isEmpty()) {
            Logger.debug("Bill not found creating new one: deposit %d, batch %d, billType %s", d.depositId, batch.batchId, billType.toString());
            LgBill ret = new LgBill(d, batch, billType);
            ret.save();
            d.bills.add(ret);
            d.save();
            return ret;
        }
        if (l.size() > 1) {
            Logger.error("There is more than one batch for deposit %d, batch %d, type %s", d.depositId, batch.batchId, billType.toString());
        }
        Logger.debug("Found bill type: deposit %d, batch %d, billType %s", d.depositId, batch.batchId, billType.toString());
        return l.get(0);
    }

    @Override
    public String toString() {
        Integer q = quantity;
        Integer d = billType.denomination;
        Integer t = quantity * billType.denomination;
        return (q.toString() + " *  $" + d.toString() + " = " + t.toString()
                + "(" + billType.currency.toString() + ")");
    }

    public Integer getTotal() {
        return quantity * billType.denomination;
    }

    public BillValue getValue() {
        return billType.getValue();
    }
}
