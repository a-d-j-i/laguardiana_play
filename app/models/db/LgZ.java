package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import models.Bill;
import models.events.ZProcessedEvent;
import models.lov.Currency;
import play.Logger;
import play.db.jpa.GenericModel;
import play.libs.F;

@Entity
@Table( name = "lg_z", schema = "public")
public class LgZ extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "z_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgZGenerator")
    @SequenceGenerator(name = "LgZGenerator", sequenceName = "lg_z_sequence")
    public Integer zId;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "close_date", length = 13)
    public Date closeDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "z")
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>(0);

    public LgZ() {
        this.creationDate = new Date();
    }

    // TODO: Search for the z that has withdraw data null.
    // Or add a new bug logging the error.
    public static LgZ getCurrentZ() {
        LgZ currentZ;
        List<LgZ> zs = LgZ.find("select bg from LgZ bg where bg.closeDate is null order by bg.creationDate desc").fetch();
        if (zs == null || zs.isEmpty()) {
            Logger.error("There's no z where to deposit, creating one!!");
            currentZ = new LgZ();
            currentZ.save();
        } else {
            if (zs.size() > 1) {
                Iterator<LgZ> i = zs.iterator();
                currentZ = i.next();
                while (i.hasNext()) {
                    LgZ toClose = i.next();
                    toClose.closeDate = new Date();
                    toClose.save();
                    Logger.error("There are more than one open z, closing the z %d", toClose.zId);
                }
            } else {
                currentZ = zs.get(0);
            }
        }
        if (currentZ == null) {
            Logger.error("APP ERROR z = null");
        }
        return currentZ;
    }

    public static LgZ rotateZ() {
        LgZ current = getCurrentZ();
        if (current.deposits.size() > 0) {
            Logger.debug("Rotating Z");
            current.closeDate = new Date();
            current.save();
            LgZ newZ = new LgZ();
            newZ.save();
        } else {
            Logger.debug("this Z is empty so don't rotate");
        }
        return current;
    }

    public static long count(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgDeposit.count("select count(z) from LgZ z where cast(creationDate as date) <= cast(? as date)", end);
        } else {
            return LgDeposit.count("select count(z) from LgZ z where "
                    + "cast(creationDate as date)  >= cast(? as date) and cast(creationDate as date) <= cast(? as date)",
                    start, end);
        }
    }

    public static JPAQuery find(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgZ.find("select z from LgZ z where cast(creationDate as date) <= cast(? as date) order by creationDate desc", end);
        } else {
            return LgZ.find("select z from LgZ z where "
                    + "cast(creationDate as date) >= cast(? as date) and cast(creationDate as date) <= cast(? as date) order by creationDate desc",
                    start, end);
        }
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgDeposit.find(
                "select z from LgZ z where "
                + "not exists ("
                + " from ZProcessedEvent e, LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea "
                + " and z.zId = e.eventSourceId"
                + " and al.event = e and ea.appId = ?"
                + ")", appId);
    }

    public static boolean process(int appId, int depositId, String resultCode) {
        LgZ z = LgZ.findById(depositId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (z == null || ea == null || z.closeDate == null) {
            return false;
        }
        ZProcessedEvent e = ZProcessedEvent.save(z, String.format("Exporting to app %d", appId));
        LgExternalAppLog el = new LgExternalAppLog(e, resultCode);
        el.successDate = new Date();
        el.setEvent(e);
        el.setExternalApp(ea);
        el.save();
        return true;
    }

    @Override
    public String toString() {
        return "LgZ{" + "zId=" + zId + ", creationDate=" + creationDate + ", closeDate=" + closeDate + '}';
    }

    public F.T5<Long, Long, Long, Map<Currency, LgDeposit.Total>, Map<Currency, Map<LgBillType, Bill>>> getTotals() {
        return LgDeposit.getTotals(this.deposits);
    }
}
