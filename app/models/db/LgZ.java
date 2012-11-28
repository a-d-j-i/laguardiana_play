package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import models.BillDeposit;
import models.EnvelopeDeposit;
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
        LgZ currentZ = null;
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

    public static F.T3<Long, Long, Long> getCurrentZTotals() {
        LgZ currentZ = getCurrentZ();
        long sum = 0;
        long envelopes = 0;
        long deposits = 0;
        for (LgDeposit d : currentZ.deposits) {
            deposits++;
            if (d instanceof BillDeposit) {
                BillDeposit bd = (BillDeposit) d;
                sum += bd.getTotal();
            } else if (d instanceof EnvelopeDeposit) {
                envelopes++;
            } else {
                Logger.error("Invalid deposit type");
            }
        }
        return new F.T3<Long, Long, Long>(sum, envelopes, deposits);
    }

    public static void rotateZ() {
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
    }

    @Override
    public String toString() {
        return "LgZ{" + "zId=" + zId + ", creationDate=" + creationDate + ", closeDate=" + closeDate + '}';
    }
}
