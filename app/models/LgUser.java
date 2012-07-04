package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_user", schema = "public" )
public class LgUser extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "user_id", unique = true, nullable = false )
    @GeneratedValue
    public int userId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "external_app_id", nullable = false )
    public LgExternalApp externalApp;
    @Column( name = "username", nullable = false, length = 128 )
    public String username;
    @Column( name = "password", nullable = false, length = 128 )
    public String password;
    @Column( name = "external_id", nullable = false, length = 32 )
    public String externalId;
    @Column( name = "locked", nullable = false, length = 1 )
    public char locked;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @Temporal( TemporalType.DATE )
    @Column( name = "end_date", nullable = true, length = 13 )
    public Date endDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user" )
    Set<LgDeposit> deposits = new HashSet<LgDeposit>( 0 );
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    @JoinTable( name = "lg_user_role", schema = "public", joinColumns = {
        @JoinColumn( name = "user_id", nullable = false, updatable = false ) }, inverseJoinColumns = {
        @JoinColumn( name = "role_id", nullable = false, updatable = false ) } )
    Set<LgRole> roles = new HashSet<LgRole>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user" )
    Set<LgUserProperty> userProperties = new HashSet<LgUserProperty>( 0 );

    public static Object connect( String username, String password ) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
