package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
abstract public class LgEvent extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "event_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer eventId;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "deposit_id")
    public LgDeposit deposit;
    @Column( name = "event_type_lov", nullable = true)
    public Integer eventTypeLov;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column( name = "message", nullable = false, length = 256)
    public String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>(0);

    @Override
    public String toString() {
        return "LgEvent{" + "eventId=" + eventId + ", deposit=" + deposit + ", eventTypeLov=" + eventTypeLov + ", creationDate=" + creationDate + ", message=" + message + '}';
    }
}
