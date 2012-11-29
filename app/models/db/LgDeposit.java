package models.db;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import models.Bill;
import models.BillDeposit;
import models.DepositEvent;
import models.DepositProcessedEvent;
import models.EnvelopeDeposit;
import models.db.LgLov.LovCol;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.db.jpa.GenericModel;
import play.db.jpa.JPABase;
import play.libs.F;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "lg_deposit", schema = "public")
@DiscriminatorColumn(name = "type", length = 32)
abstract public class LgDeposit extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "deposit_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgDepositGenerator")
    @SequenceGenerator(name = "LgDepositGenerator", sequenceName = "lg_deposit_sequence")
    public Integer depositId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public LgUser user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_id", nullable = false)
    public LgBag bag;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "z_id", nullable = false)
    public LgZ z;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date", length = 13)
    public Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finish_date", length = 13)
    public Date finishDate;
    @Column(name = "user_code", length = 128)
    public String userCode;
    @Column(name = "user_code_lov")
    @LovCol(DepositUserCodeReference.class)
    public Integer userCodeLov;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit")
    public Set<LgEnvelope> envelopes = new HashSet<LgEnvelope>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit")
    public Set<LgBill> bills = new HashSet<LgBill>(0);

    public LgDeposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        this.bag = LgBag.getCurrentBag();
        this.z = LgZ.getCurrentZ();
        this.user = user;
        this.userCode = userCode;

        if (userCodeData != null) {
            this.userCodeLov = userCodeData.numericId;
        }
        this.creationDate = new Date();
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgDeposit.find(
                "select d from LgDeposit d where "
                + " finishDate is not null "
                + "and not exists ("
                + " from DepositProcessedEvent e, LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea "
                + " and d.depositId = e.eventSourceId"
                + " and al.event = e and ea.appId = ?"
                + ")", appId);
    }

    public static boolean process(int appId, int depositId, String resultCode) {
        LgDeposit d = LgDeposit.findById(depositId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (d == null || ea == null) {
            return false;
        }
        DepositProcessedEvent e = DepositProcessedEvent.save(d, String.format("Exporting to app %d", appId));
        LgExternalAppLog el = new LgExternalAppLog(e, resultCode);
        el.successDate = new Date();
        el.setEvent(e);
        el.setExternalApp(ea);
        el.save();
        return true;
    }

    public void addBatch(LgBatch batch) {
        for (LgBill bill : batch.bills) {
            bills.add(bill);
            bill.deposit = this;
        }
    }

    public void addEnvelope(LgEnvelope envelope) {
        envelopes.add(envelope);
        envelope.deposit = this;
    }

    @Override
    public <T extends JPABase> T save() {
        boolean mustSave = (!JPABase.em().contains(this));
        T ret = super.save();
        if (mustSave) {
            DepositEvent.save(user, this, String.format("Deposit changed by userId : %d", user.userId));
        }
        return ret;
    }

    public LgLov findUserCodeLov() {
        return DepositUserCodeReference.findByNumericId(userCodeLov);
    }

    static public F.T4<Long, Long, Long, Collection<Bill>> getDepositsTotals(Set<LgDeposit> deps) {
        long sum = 0;
        long envelopes = 0;
        long deposits = 0;
        Map<LgBillType, Bill> totalBills = new HashMap<LgBillType, Bill>();
        for (LgDeposit d : deps) {
            deposits++;
            if (d instanceof BillDeposit) {
                BillDeposit bd = (BillDeposit) d;
                for (LgBill b : bd.bills) {
                    Bill bill;
                    if (totalBills.containsKey(b.billType)) {
                        bill = totalBills.get(b.billType);
                    } else {
                        bill = new Bill(b.billType);
                    }
                    bill.q += b.quantity;
                    totalBills.put(b.billType, bill);
                }
                sum += bd.getTotal();
            } else if (d instanceof EnvelopeDeposit) {
                envelopes++;
            } else {
                Logger.error("Invalid deposit type");
            }
        }
        return new F.T4<Long, Long, Long, Collection<Bill>>(sum, envelopes, deposits, totalBills.values());
    }

    @Override
    public String toString() {
        return "LgDeposit{" + "depositId=" + depositId + ", user=" + user + ", creationDate=" + creationDate + ", startDate=" + startDate + ", finishDate=" + finishDate + ", userCode=" + userCode + ", userCodeLov=" + userCodeLov + ", bag=" + bag + ", z=" + z + '}';
    }
}
