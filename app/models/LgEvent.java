package models;

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
    int eventId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id" )
    LgDeposit lgDeposit;
    @Column( name = "event_type_lov", nullable = false )
    int eventTypeLov;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    Date creationDate;
    @Column( name = "message", nullable = false, length = 256 )
    String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgEvent" )
    Set<LgExternalAppLog> lgExternalAppLogs = new HashSet<LgExternalAppLog>( 0 );
}
