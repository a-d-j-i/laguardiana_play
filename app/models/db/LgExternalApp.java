package models.db;

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
    @GeneratedValue
    public int externalAppId;
    @Column( name = "name", nullable = false, length = 128 )
    public String name;
    @Column( name = "retry_interval", nullable = false )
    public int retryInterval;
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "execution_time", nullable = false, length = 29 )
    public Date executionTime;
    @Column( name = "presicion", nullable = false )
    public int presicion;
    @Column( name = "interval", nullable = false )
    public int interval;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "externalApp" )
    public Set<LgUser> users = new HashSet<LgUser>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "externalApp" )
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>( 0 );
    
    @Override
    public String toString() {
        return name;
    }
}
