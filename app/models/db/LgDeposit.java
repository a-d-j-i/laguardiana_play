package models.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import models.db.LgLov.LovCol;
import models.events.DepositEvent;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.db.jpa.GenericModel;
import play.db.jpa.JPABase;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "lg_deposit", schema = "public")
@DiscriminatorColumn(name = "type", length = 32)
abstract public class LgDeposit extends GenericModel implements java.io.Serializable {

    public enum FinishCause {

        FINISH_CAUSE_OK,
        FINISH_CAUSE_ERROR,
        FINISH_CAUSE_CANCEL,
        FINISH_CAUSE_BAG_REMOVED,
        FINISH_CAUSE_BAG_FULL,;

        @Override
        public String toString() {
            return name();
        }
    };
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
    // Closed correctly canceld or not.
    @Column(name = "close_date", length = 13)
    public Date closeDate;
    @Temporal(TemporalType.TIMESTAMP)
    // not currently processed.
    @Column(name = "finish_date", length = 13)
    public Date finishDate;
    @Column(name = "finish_cause", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    public FinishCause finishCause;
    @Column(name = "confirm_date", length = 13)
    public Date confirmDate;
    @Column(name = "user_code", length = 128)
    public String userCode;
    @Column(name = "user_code_lov")
    @LovCol(DepositUserCodeReference.class)
    public Integer userCodeLov;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit")
    public Set<LgEnvelope> envelopes = new HashSet<LgEnvelope>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "deposit")
    public Set<LgBill> bills = new HashSet<LgBill>(0);

    public LgDeposit(LgUser user, String userCode, Integer userCodeLovId) {
        this.bag = LgBag.getCurrentBag();
        this.z = LgZ.getCurrentZ();
        this.user = user;
        this.userCodeLov = userCodeLovId;
        this.creationDate = new Date();
    }
//
//    public LgDeposit(LgDeposit refDeposit) {
//        this((LgUser) LgUser.findById(refDeposit.user.userId), (Currency) Currency.findById(refDeposit.currency.lovId),
//                refDeposit.userCode, refDeposit.userCodeLov);
//    }

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

    public static LgDeposit getCurrentDeposit() {
        List< LgDeposit> unfinished = LgDeposit.find("select d from LgDeposit d where finishDate is null order by finishDate desc").fetch();
        if (unfinished.isEmpty()) {
            return null;
        }
        for (int i = 1; i < unfinished.size(); i++) {
            LgDeposit l = unfinished.get(i);
            Logger.debug("Closing deposit %d unfinished", l.depositId);
            DepositEvent.save(l.user, l, String.format("Deposit finished automatically"));
            l.finishDate = new Date();
            l.save();
        }
        return unfinished.get(0);

    }

    public boolean isFinished() {
        return finishDate != null;
    }

    public void closeDeposit(LgDeposit.FinishCause finishCause) {
        finishDate = new Date();
        this.finishCause = finishCause;
        save();
    }

    /*    public static void closeUnfinished() {
     List< LgDeposit> unfinished = LgDeposit.find("select d from LgDeposit d where finishDate is null").fetch();
     for (LgDeposit l : unfinished) {
     Logger.debug("Closing deposit %d unfinished", l.depositId);
     DepositEvent.save(l.user, l, String.format("Deposit finished automatically"));
     l.finishDate = new Date();
     l.save();
     }
     }
     */
    public static JPAQuery findUnprocessed(int appId) {
        return LgDeposit.find(
                "select d from LgDeposit d where "
                + " finishDate is not null "
                + "and not exists ("
                + " from LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea and al.logType = ?"
                + " and d.depositId = al.logSourceId"
                + " and ea.appId = ?"
                + ")", LgExternalAppLog.LOG_TYPES.DEPOSIT.name(), appId);
    }

    public static boolean process(int appId, int depositId, String resultCode) {
        LgDeposit d = LgDeposit.findById(depositId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (d == null || ea == null) {
            return false;
        }

        LgExternalAppLog el = new LgExternalAppLog(LgExternalAppLog.LOG_TYPES.DEPOSIT, d.depositId, resultCode, String.format("Exporting to app %d", appId));
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
        return "LgDeposit{" + "depositId=" + depositId + ", user=" + user + ", creationDate=" + creationDate + ", startDate=" + startDate + ", finishDate=" + finishDate + ", closeDate=" + closeDate + ", userCode=" + userCode + ", userCodeLov=" + userCodeLov + ", bag=" + bag + ", z=" + z + '}';
    }

    abstract public void setRenderArgs(Map args);

    abstract public void print(boolean reprint);

    abstract public String getDetailView();

    public interface DepositVisitor {

        void visit(LgDeposit item);
    }

    public static void visitDeposits(Set<LgDeposit> deps, DepositVisitor visitor) {
        for (LgDeposit d : deps) {
            visitor.visit(d);
        }
    }

}
