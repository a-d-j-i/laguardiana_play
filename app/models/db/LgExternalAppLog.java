package models.db;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_external_app_log", schema = "public" )
public class LgExternalAppLog extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "log_id", unique = true, nullable = false )
    @GeneratedValue
    public int logId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "external_app_id", nullable = false )
    public LgExternalApp externalApp;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "event_id", nullable = false )
    public LgEvent event;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @Column( name = "result_code", nullable = false, length = 32 )
    public String resultCode;
    @Temporal( TemporalType.DATE )
    @Column( name = "success_date", nullable = false, length = 13 )
    public Date successDate;
}
