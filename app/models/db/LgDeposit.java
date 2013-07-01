package models.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import models.BillDAO;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgLov.LovCol;
import models.events.DepositEvent;
import models.lov.Currency;
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

    private static String getFindQuery(String query, List<Object> args, Date start, Date end, Integer bagId, Integer zId) {
        if (end == null) {
            end = new Date();
        }

        String whereClause = " cast(creationDate as date) <= cast(? as date)";
        args.add(end);
        if (start != null) {
            whereClause += " and cast(creationDate as date) >= cast(? as date)";
            args.add(start);
        }
        if (bagId != null) {
            whereClause += " and bag.bagId = ?";
            args.add(bagId);
        }
        if (zId != null) {
            whereClause += " and z.zId = ?";
            args.add(zId);
        }
        return query + " from LgDeposit d where " + whereClause;
    }

    public static long count(Date start, Date end, Integer bagId, Integer zId) {
        List<Object> args = new ArrayList<Object>();
        String query = getFindQuery("select count(d)", args, start, end, bagId, zId);
        return LgDeposit.count(query, args.toArray());
    }

    public static JPAQuery find(Date start, Date end, Integer bagId, Integer zId) {
        List<Object> args = new ArrayList<Object>();
        String query = getFindQuery("select d", args, start, end, bagId, zId) + " order by creationDate desc";
        return LgDeposit.find(query, args.toArray());
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgDeposit.find(
                "select d from LgDeposit d where "
                + " finishDate is not null "
                + "and not exists ("
                + " from LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea "
                + " and d.depositId = al.logSourceId"
                + " and ea.appId = ?"
                + ")", appId);
    }

    public static boolean process(int appId, int depositId, String resultCode) {
        LgDeposit d = LgDeposit.findById(depositId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (d == null || ea == null) {
            return false;
        }

        LgExternalAppLog el = new LgExternalAppLog(d, resultCode, String.format("Exporting to app %d", appId));
        el.successDate = new Date();
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

    public String findUserCode() {
        if (userCodeLov == null) {
            return null;
        }
        DepositUserCodeReference d = DepositUserCodeReference.findByNumericId(userCodeLov);
        if (d == null | d.description == null) {
            return null;
        }
        return d.description;
    }

    @Override
    public String toString() {
        return "LgDeposit{" + "depositId=" + depositId + ", user=" + user + ", creationDate=" + creationDate + ", startDate=" + startDate + ", finishDate=" + finishDate + ", userCode=" + userCode + ", userCodeLov=" + userCodeLov + ", bag=" + bag + ", z=" + z + '}';
    }

    abstract public void setRenderArgs(Map args);

    abstract public void print(boolean reprint);

    abstract public String getDetailView();

    public static class Total {

        public long a = 0;
        public long q = 0;
    }

    public static F.T5<Long, Long, Long, Map<Currency, Total>, Map<Currency, Map<LgBillType, BillDAO>>> getTotals(Set<LgDeposit> deps) {
        long envelopes = 0;
        long deposits = 0;
        long bills = 0;
        Map<Currency, Map<LgBillType, BillDAO>> totals = new HashMap();
        Map<Currency, Total> qaTotals = new HashMap();
        for (LgDeposit d : deps) {
            if (d.finishDate == null) {
                continue;
            }
            deposits++;
            if (d instanceof BillDeposit) {
                BillDeposit bd = (BillDeposit) d;
                for (LgBill b : bd.bills) {
                    bills += b.quantity;
                    Currency c = b.billType.getCurrency();
                    Total at = qaTotals.get(c);
                    if (at == null) {
                        at = new Total();
                    }
                    at.a += b.getTotal();
                    at.q += b.quantity;
                    qaTotals.put(c, at);

                    Map<LgBillType, BillDAO> ct = totals.get(c);
                    if (ct == null) {
                        ct = new HashMap();
                    }
                    BillDAO bill = ct.get(b.billType);
                    if (bill == null) {
                        bill = new BillDAO(b.billType);
                    }
                    bill.q += b.quantity;
                    totals.put(c, ct);
                    ct.put(b.billType, bill);
                }
            } else if (d instanceof EnvelopeDeposit) {
                envelopes++;
            } else {
                Logger.error("Invalid deposit type");
            }
        }
        return new F.T5<Long, Long, Long, Map<Currency, Total>, Map<Currency, Map<LgBillType, BillDAO>>>(deposits, envelopes, bills, qaTotals, totals);
    }
}
