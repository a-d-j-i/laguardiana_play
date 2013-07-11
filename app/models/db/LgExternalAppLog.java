package models.db;

import java.util.Date;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table( name = "lg_external_app_log", schema = "public")
public class LgExternalAppLog extends GenericModel implements java.io.Serializable {
    public enum LOG_TYPES {
        BAG,
        DEPOSIT,
        Z,
        EVENT,
    };
    @Id
    @Column( name = "log_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgExternalAppLogGenerator")
    @SequenceGenerator(name = "LgExternalAppLogGenerator", sequenceName = "lg_external_app_log_sequence")
    public Integer logId;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "external_app_id", nullable = false)
    public LgExternalApp externalApp;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "success_date", nullable = false, length = 13)
    public Date successDate;
    @Column( name = "result_code", nullable = false, length = 32)
    public String resultCode;
    @Column( name = "log_type", nullable = false, length = 128)
    public String logType;
    @Column(name = "log_source_id", nullable = true)
    public Integer logSourceId;
    @Column( name = "message", nullable = true, length = 256)
    public String message;

    public LgExternalAppLog(LgBag b, String resultCode, String message) {
        this(LOG_TYPES.BAG, b.bagId, resultCode, message);
    }

    public LgExternalAppLog(LgDeposit d, String resultCode, String message) {
        this(LOG_TYPES.DEPOSIT, d.depositId, resultCode, message);
    }

    public LgExternalAppLog(LgZ z, String resultCode, String message) {
        this(LOG_TYPES.Z, z.zId, resultCode, message);
    }

    public LgExternalAppLog(LgEvent e, String resultCode, String message) {
        this(LOG_TYPES.EVENT, e.eventId, resultCode, message);
    }

    public LgExternalAppLog(LOG_TYPES logType, Integer logSourceId, String resultCode, String message) {
        this.logType = logType.name();
        this.logSourceId = logSourceId;
        this.resultCode = resultCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return "LgExternalAppLog{" + "logId=" + logId + ", externalApp=" + externalApp + ", creationDate=" + creationDate + ", successDate=" + successDate + ", resultCode=" + resultCode + ", logType=" + logType + ", logSourceId=" + logSourceId + ", message=" + message + '}';
    }

    public void setExternalApp(LgExternalApp ea) {
        if (ea != null) {
            this.externalApp = ea;
            ea.externalAppLogs.add(this);
        } else {
            Logger.error("LgExternalAppLog LgExternalApp null");
        }

    }
}
