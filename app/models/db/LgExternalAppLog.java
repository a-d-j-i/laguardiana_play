package models.db;

import java.util.Date;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_external_app_log", schema = "public")
public class LgExternalAppLog extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "log_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer logId;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "external_app_id", nullable = false)
    public LgExternalApp externalApp;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "event_id", nullable = false)
    public LgEvent event;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column( name = "result_code", nullable = false, length = 32)
    public String resultCode;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "success_date", nullable = false, length = 13)
    public Date successDate;

    public LgExternalAppLog(LgEvent event, String resultCode) {
        this.event = event;
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "LgExternalAppLog{" + "logId=" + logId + ", externalApp=" + externalApp + ", event=" + event + ", creationDate=" + creationDate + ", resultCode=" + resultCode + ", successDate=" + successDate + '}';
    }

    public void setEvent(LgEvent e) {
        if (e != null) {
            this.event = e;
            e.externalAppLogs.add(this);
        } else {
            Logger.error("LgExternalAppLog event null");
        }
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
