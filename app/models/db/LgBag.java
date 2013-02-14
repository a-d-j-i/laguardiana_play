package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import models.BagEvent;
import models.BagProcessedEvent;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bag", schema = "public")
public class LgBag extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bag_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgBagGenerator")
    @SequenceGenerator(name = "LgBagGenerator", sequenceName = "lg_bag_sequence")
    public Integer bagId;
    @Column( name = "bag_code", nullable = false, length = 128)
    public String bagCode;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "withdraw_date", length = 13)
    public Date withdrawDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "bag")
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>(0);

    public LgBag(String bagCode) {
        this.bagCode = bagCode;
        this.creationDate = new Date();
    }

    public static long count(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgDeposit.count("select count(b) from LgBag b where cast(creationDate as date) <= cast(? as date)", end);
        } else {
            return LgDeposit.count("select count(b) from LgBag b where "
                    + "cast(creationDate as date)  >= cast(? as date) and cast(creationDate as date) <= cast(? as date)",
                    start, end);
        }
    }

    public static JPAQuery find(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgBag.find("select b from LgBag b where cast(creationDate as date) <= cast(? as date) order by bagId", end);
        } else {
            return LgBag.find("select b from LgBag b where "
                    + "cast(creationDate as date) >= cast(? as date) and cast(creationDate as date) <= cast(? as date) order by bagId",
                    start, end);
        }
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgDeposit.find(
                "select b from LgBag b where "
                + "not exists ("
                + " from BagProcessedEvent e, LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea "
                + " and b.bagId = e.eventSourceId"
                + " and al.event = e and ea.appId = ?"
                + ")", appId);
    }

    public static boolean process(int appId, int depositId, String resultCode) {
        LgBag b = LgBag.findById(depositId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (b == null || ea == null || b.withdrawDate == null) {
            return false;
        }
        BagProcessedEvent e = BagProcessedEvent.save(b, String.format("Exporting to app %d", appId));
        LgExternalAppLog el = new LgExternalAppLog(e, resultCode);
        el.successDate = new Date();
        el.setEvent(e);
        el.setExternalApp(ea);
        el.save();
        return true;
    }

    // TODO: Search for the bag that has withdraw data null.
    // Or add a new bug logging the error.
    public static LgBag getCurrentBag() {
        LgBag currentBag;
        List<LgBag> bags = LgBag.find("select bg from LgBag bg where bg.withdrawDate is null order by bg.creationDate desc").fetch();
        if (bags == null || bags.isEmpty()) {
            Logger.error("There's no bag where to deposit, creating one!!");
            BagEvent.save(null, "There is no bag to deposit creating one");
            currentBag = new LgBag("AUTOMATIC_BY_APP");
            currentBag.save();
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

    public static void rotateBag(boolean force) {

        LgBag current = getCurrentBag();

        if (current.deposits.size() > 0 || force) {
            current.withdrawDate = new Date();
            BagEvent.save(current, "Closing bag");
            current.save();
            LgBag newBag = new LgBag("AUTOMATIC_ROTATED_BY_APP");
            newBag.save();
            BagEvent.save(newBag, "Opening new bag");
        } else {
            Logger.debug("this bag is empty so I'm going to reuse it");
        }

    }

    @Override
    public String toString() {
        return "LgBag{" + "bagId=" + bagId + ", bagCode=" + bagCode + ", creationDate=" + creationDate + ", withdrawDate=" + withdrawDate + '}';
    }
}
