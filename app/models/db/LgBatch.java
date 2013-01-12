package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_batch", schema = "public")
public class LgBatch extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "batch_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgBatchGenerator")
    @SequenceGenerator(name = "LgBatchGenerator", sequenceName = "lg_batch_sequence")
    public Integer batchId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finish_date", length = 13)
    public Date finishDate;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "batch")
    public Set<LgBill> bills = new HashSet<LgBill>(0);

    public LgBatch() {
        this.creationDate = new Date();
    }

    public void addBill(LgBill b) {
        b.batch = this;
        bills.add(b);
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
