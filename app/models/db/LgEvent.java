package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_event", schema = "public" )
public class LgEvent extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "event_id", unique = true, nullable = false )
    @GeneratedValue
    public int eventId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id" )
    public LgDeposit deposit;
    @Column( name = "event_type_lov", nullable = false )
    public int eventTypeLov;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @Column( name = "message", nullable = false, length = 256 )
    public String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event" )
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>( 0 );
}
