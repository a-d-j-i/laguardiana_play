package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import models.Event;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bag", schema = "public")
public class LgBag extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bag_id", unique = true, nullable = false)
    @GeneratedValue
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

    // TODO: Search for the bag that has withdraw data null.
    // Or add a new bug logging the error.
    public static LgBag getCurrentBag() {
        LgBag currentBag = null;
        List<LgBag> bags = LgBag.find("select bg from LgBag bg where bg.withdrawDate is null order by bg.creationDate desc").fetch();
        if (bags == null || bags.isEmpty()) {
            Logger.error("There's no bag where to deposit, creating one!!");
            Event.save(null, Event.Type.INVALID_BAG, "There is no bag to deposit creating one");
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
                    Event.save(null, Event.Type.INVALID_BAG, String.format("There are more than one open bag, closing the bag %d", toClose.bagId));
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

    @Override
    public String toString() {
        return "LgBag{" + "bagId=" + bagId + ", bagCode=" + bagCode + ", creationDate=" + creationDate + ", withdrawDate=" + withdrawDate + '}';
    }
}
