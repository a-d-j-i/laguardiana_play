package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import models.BillDeposit;
import models.Configuration;
import models.EnvelopeDeposit;
import models.ItemQuantity;
import models.ReportTotals;
import models.events.BagEvent;
import play.Logger;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;

@Entity
@Table(name = "lg_bag", schema = "public")
public class LgBag extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "bag_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgBagGenerator")
    @SequenceGenerator(name = "LgBagGenerator", sequenceName = "lg_bag_sequence")
    public Integer bagId;
    @Column(name = "bag_code", nullable = false, length = 128)
    public String bagCode;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Column(name = "placement_date", nullable = true, length = 13)
    public Date placementDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "withdraw_date", length = 13)
    public Date withdrawDate;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "bag")
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "currentBag")
    public Set<LgDevice> ioboards;
    @Transient
    transient public String withdrawUser;
//    @Transient
//    transient private Long totalAmount = null;

    public LgBag(String bagCode) {
        this.bagCode = bagCode;
        this.creationDate = new Date();
    }

    @PostLoad
    public void postLoad() {
        this.withdrawUser = Configuration.getWithdrawUser();
    }

    /*    public Long getTotalAmount() {
     if (totalAmount == null) {
     totalAmount = BillDeposit.find("select sum(b.quantity * bt.denomination) "
     + " from BillDeposit d, LgBill b, LgBillType bt, LgBag bg "
     + " where b.deposit = d and b.billType = bt and bg = d.bag"
     + " and bg.bagId= ?", bagId).first();
     }
     if (totalAmount == null) {
     totalAmount = new Long(0);
     }
     return totalAmount;
     }
     */
    public static long count(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgBag.count("select count(b) from LgBag b where cast(creationDate as date) <= cast(? as date)", end);
        } else {
            return LgBag.count("select count(b) from LgBag b where "
                    + "cast(creationDate as date)  >= cast(? as date) and cast(creationDate as date) <= cast(? as date)",
                    start, end);
        }
    }

    public static JPAQuery find(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgBag.find("select b from LgBag b where cast(creationDate as date) <= cast(? as date) order by creationDate desc", end);
        } else {
            return LgBag.find("select b from LgBag b where "
                    + "cast(creationDate as date) >= cast(? as date) and cast(creationDate as date) <= cast(? as date) order by creationDate desc",
                    start, end);
        }
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgBag.find(
                "select b from LgBag b where b.withdrawDate is not null and "
                + "not exists ("
                + " from LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea and al.logType = ?"
                + " and b.bagId = al.logSourceId"
                + " and ea.appId = ?"
                + ")", LgExternalAppLog.LOG_TYPES.BAG.name(), appId);
    }

    public static boolean process(int appId, int bagId, String resultCode) {
        LgBag b = LgBag.findById(bagId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (b == null || ea == null || b.withdrawDate == null) {
            return false;
        }
        LgExternalAppLog el = new LgExternalAppLog(LgExternalAppLog.LOG_TYPES.BAG, b.bagId, resultCode, String.format("Exporting to app %d", appId));
        el.successDate = new Date();
        el.setExternalApp(ea);
        el.save();
        return true;
    }

    // TODO: Search for the bag that has withdraw data null.
    // Or add a new bag logging the error.
    // There must allways be a bag because if the ioboard is ignored I can't have a withdraw and placement date
    // but I need a bug to relate the deposits.
    public static LgBag getCurrentBag() {
        LgBag currentBag;
        List<LgBag> bags = LgBag.find("select bg from LgBag bg where bg.withdrawDate is null order by bg.creationDate desc").fetch();
        if (bags == null || bags.isEmpty()) {
            Logger.error("There's no bag where to deposit, creating one!!");
            BagEvent.save(null, "There is no bag to deposit creating one");
            currentBag = new LgBag("AUTOMATIC_BY_APP");
            currentBag.save();
            JPA.em().getTransaction().commit();
        } else {
            if (bags.size() > 1) {
                Iterator<LgBag> i = bags.iterator();
                currentBag = i.next();
                while (i.hasNext()) {
                    LgBag toClose = i.next();
                    toClose.withdrawDate = new Date();
                    toClose.save();
                    Logger.error("There are more than one open bag, closing the bag %d", toClose.bagId);
                    BagEvent.save(toClose, String.format("There are more than one open bag, closing the bag %d", toClose.bagId));
                }
            } else {
                currentBag = bags.get(0);
            }
        }
        if (currentBag == null) {
            Logger.error("APP ERROR bag = null");
        }
        return currentBag;
    }

    synchronized public static void withdrawBag(boolean byIoBoard) {

        LgBag current = getCurrentBag();
        // If the bug was not placed by the ioboard then it can be withdrawn by the ioboard.
        if (current.placementDate == null && byIoBoard) {
            return;
        }
        // last bag was removed and is still no placed;
        /*Logger.debug("--------------------------> rotate bag current %s placement %s withdraw %s",
         current.bagId.toString(), current.placementDate, current.withdrawDate);*/
        if (current.deposits.size() > 0 || byIoBoard) {
            current.withdrawDate = new Date();
            if (byIoBoard) {
                BagEvent.save(current, "Closing bag byIoBoard");
            } else {
                BagEvent.save(current, "Closing bag manually");
            }
            current.save();
            Logger.debug("BAG REMOVED %d", current.bagId);
            LgBag newBag = new LgBag("AUTOMATIC_ROTATED_BY_APP");
            newBag.save();
            BagEvent.save(newBag, "Opening new bag");
            Logger.debug("BAG CREATED %d", newBag.bagId);
            JPA.em().getTransaction().commit();
        } else {
            Logger.warn("this bag (%s) is empty so I'm going to reuse it", current.bagId.toString());
        }
    }

    synchronized public static void placeBag() {
        Date currDate = new Date();
        LgBag current = getCurrentBag();
        if (current.placementDate != null) {
            Logger.warn("Cant place a bag (%s) now %s that was allready put in date %s",
                    current.bagId.toString(), currDate.toString(), current.placementDate.toString());
            return;
        }
        current.placementDate = currDate;
        current.save();
        BagEvent.save(current, "Bag placed");
        JPA.em().getTransaction().commit();
        Logger.debug("BAG PLACED %d", current.bagId);
    }

    @Override
    public String toString() {
        return "LgBag{" + "bagId=" + bagId + ", bagCode=" + bagCode + ", creationDate=" + creationDate + ", placementDate=" + placementDate + ", withdrawDate=" + withdrawDate + '}';
    }

    public ItemQuantity getItemQuantity() {
        return getItemQuantity(null);
    }

    public ItemQuantity getItemQuantity(final Integer currentDepositId) {
        final ItemQuantity ret = new ItemQuantity();
        LgDeposit.visitDeposits(deposits, new LgDeposit.DepositVisitor() {
            public void visit(LgDeposit item) {
                if (item.depositId.equals(currentDepositId) || item.finishCause != LgDeposit.FinishCause.FINISH_CAUSE_CANCEL) {
                    if (item instanceof EnvelopeDeposit) {
                        ret.envelopes++;
                    } else if (item instanceof BillDeposit) {
                        for (LgBill b : item.bills) {
                            ret.bills += b.quantity;
                        }
                    } else {
                        Logger.error(String.format("Invalid deposit type %s", item.getClass()));
                    }
                }
            }
        });
        return ret;
    }

    public ReportTotals getTotals() {
        ReportTotals totals = new ReportTotals();
        return totals.getTotals(deposits);
    }

}
