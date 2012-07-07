package models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
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
    private String password;
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
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>( 0 );
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    @JoinTable( name = "lg_user_role", schema = "public", joinColumns = {
        @JoinColumn( name = "user_id", nullable = false, updatable = false ) }, inverseJoinColumns = {
        @JoinColumn( name = "role_id", nullable = false, updatable = false ) } )
    public Set<LgRole> roles = new HashSet<LgRole>( 0 );
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user" )
    public Set<LgUserProperty> userProperties = new HashSet<LgUserProperty>( 0 );

    class PermsKey {

        String resource;
        String operation;

        public PermsKey( String resource, String operation ) {
            this.resource = resource;
            this.operation = operation;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final PermsKey other = ( PermsKey ) obj;
            if ( ( this.resource == null ) ? ( other.resource != null ) : !this.resource.equals( other.resource ) ) {
                return false;
            }
            if ( ( this.operation == null ) ? ( other.operation != null ) : !this.operation.equals( other.operation ) ) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + ( this.resource != null ? this.resource.hashCode() : 0 );
            hash = 41 * hash + ( this.operation != null ? this.operation.hashCode() : 0 );
            return hash;
        }
    }
    @Transient
    private Map<PermsKey, LgAclRule> perms = new HashMap<PermsKey, LgAclRule>();

    public static Object connect( String username, String password ) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public boolean authenticate( String token ) {
        if ( password == null || !password.equals( token ) ) {
            return false;
        }
        if ( endDate.before(  new Date() ) ) {
            return false;
        }
        return true;
    }

    private void writeObject( ObjectOutputStream out ) throws IOException {
        password = null;
        out.defaultWriteObject();
    }

    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    @PostLoad
    public void postLoad() {
        // TODO: Implement that with a query.
        for ( LgRole rol : roles ) {
            for ( LgAclRule rule : rol.aclRules ) {
                PermsKey k = new PermsKey( rule.resource.name, rule.operation );
                perms.put( k, rule );
            }
        }
    }

    public boolean checkPermission( String resource, String operation ) {
        PermsKey k = new PermsKey( resource, operation );
        if ( !perms.containsKey( k ) ) {
            return false;
        }
        LgAclRule rule = perms.get( k );
        if ( rule == null ) {
            return false;
        }
        if ( rule.permission.equalsIgnoreCase( "ALLOW" ) ) {
            return true;
        } else if ( rule.permission.equalsIgnoreCase( "DENY" ) ) {
            return false;
        }
        return false;
    }
    
    public String toString() {
        return username;
    }
}
