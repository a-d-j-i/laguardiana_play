package models.db;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_event", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", length = 32)
abstract public class LgEvent extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "event_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgEventGenerator")
    @SequenceGenerator(name = "LgEventGenerator", sequenceName = "lg_event_sequence")
    public Integer eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public LgUser user;
    @Column(name = "event_source_id", nullable = true)
    public Integer eventSourceId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column(name = "message", nullable = true, length = 512)
    public String message;

    public LgEvent(LgUser user, Integer eventSourceId, String message) {
        this.user = user;
        if (message.length() > 500) {
            this.message = message.substring(0, 500) + "...";
        } else {
            this.message = message;
        }
        this.eventSourceId = eventSourceId;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "eventId=" + eventId + ", user=" + user + ", eventSourceId=" + eventSourceId + ", creationDate=" + creationDate + ", message=" + message + '}';
    }

    public static long count(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgEvent.count("select count(e) from LgEvent e where cast(creationDate as date) <= cast(? as date)", end);
        } else {
            return LgEvent.count("select count(e) from LgEvent e where "
                    + "cast(creationDate as date)  >= cast(? as date) and cast(creationDate as date) <= cast(? as date)",
                    start, end);
        }
    }

    public static JPAQuery find(Date start, Date end) {
        if (end == null) {
            end = new Date();
        }
        if (start == null) {
            return LgEvent.find("select e from LgEvent e where cast(creationDate as date) <= cast(? as date) order by creationDate desc", end);
        } else {
            return LgEvent.find("select e from LgEvent e where "
                    + "cast(creationDate as date) >= cast(? as date) and cast(creationDate as date) <= cast(? as date) order by creationDate desc",
                    start, end);
        }
    }

    public static JPAQuery findUnprocessed(int appId) {
        return LgEvent.find(
                "select e from LgEvent e where "
                + "not exists ("
                + " from LgExternalAppLog al, LgExternalApp ea"
                + " where al.externalApp = ea and al.logType = ?"
                + " and e.eventId = al.logSourceId"
                + " and ea.appId = ?"
                + ")", LgExternalAppLog.LOG_TYPES.EVENT.name(), appId);
    }

    public static boolean process(int appId, int eventId, String resultCode) {
        LgEvent e = LgEvent.findById(eventId);
        LgExternalApp ea = LgExternalApp.findByAppId(appId);
        if (e == null || ea == null) {
            return false;
        }
        LgExternalAppLog el = new LgExternalAppLog(LgExternalAppLog.LOG_TYPES.EVENT, e.eventId, resultCode, String.format("Exporting to app %d", appId));
        el.successDate = new Date();
        el.setExternalApp(ea);
        el.save();
        return true;
    }

}
