package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import models.lov.Currency;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_batch", schema = "public")
public class LgBatch extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "batch_id", unique = true, nullable = false)
    @GeneratedValue
    public int batchId;
    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "batch")
    public Set<LgBill> bills = new HashSet<LgBill>(0);

    public LgBatch() {
        this.creationDate = new Date();
    }

    public void addBill(LgBill b) {
        b.batch = this;
        bills.add(b);
    }

    public static LgBatch MakeRandom(LgDeposit deposit) {
        Currency billLov = Currency.findByTextId("Pesos Argentinos");
        if (billLov == null) {
            Logger.error("no bill code for pesos argentinos!");
            return null;
        }
        int pesos = billLov.numericId;
        LgBatch batch = new LgBatch();
        LgBillType billType = LgBillType.find(5, pesos);
        //thisb.save();
        for (int i = 0; i < 4; i = i + 1) {
            LgBill bill = new LgBill(i, billType);
            batch.addBill(bill);
            Logger.info(" created: %s", bill.toString());
            //bill.save();
        }
        Logger.info(" bills: %d", batch.bills.size());
        return batch;
    }

    @Override
    public String toString() {
        long sum = 0;
        Integer depositId = 0;
        String result = "[";
        for (LgBill bill : bills) {
            if (bill.quantity > 0) {
                depositId = bill.deposit.depositId;
                sum += bill.quantity * bill.billType.denomination;
                result += bill.toString();
            }
        }
        return result + " == " + Long.toString(sum) + "]" + " {" + depositId.toString() + "}";
    }
}
