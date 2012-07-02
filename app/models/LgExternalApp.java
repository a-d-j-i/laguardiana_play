package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_external_app", schema = "public" )
public class LgExternalApp extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "external_app_id", unique = true, nullable = false )
    int externalAppId;
    @Column( name = "name", nullable = false, length = 128 )
    String name;
    @Column( name = "retry_interval", nullable = false )
    int retryInterval;
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "execution_time", nullable = false, length = 29 )
    Date executionTime;
    @Column( name = "presicion", nullable = false )
    int presicion;
    @Column( name = "interval", nullable = false )
    int interval;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgExternalApp" )
    Set<LgUser> lgUsers = new HashSet<LgUser>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgExternalApp" )
    Set<LgExternalAppLog> lgExternalAppLogs = new HashSet<LgExternalAppLog>( 0 );
}
