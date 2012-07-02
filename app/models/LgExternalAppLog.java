package models;

import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_external_app_log", schema = "public" )
public class LgExternalAppLog extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "log_id", unique = true, nullable = false )
    int logId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "external_app_id", nullable = false )
    LgExternalApp lgExternalApp;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "event_id", nullable = false )
    LgEvent lgEvent;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    Date creationDate;
    @Column( name = "result_code", nullable = false, length = 32 )
    String resultCode;
    @Temporal( TemporalType.DATE )
    @Column( name = "success_date", nullable = false, length = 13 )
    Date successDate;
}
