package models;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_role", schema = "public" )
public class LgRole extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "role_id", unique = true, nullable = false )
    @GeneratedValue
    public int roleId;
    @Column( name = "name", nullable = false, length = 64 )
    public String name;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgRole" )
    public Set<LgAclRule> aclRules = new HashSet<LgAclRule>( 0 );
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgRoles" )
    public Set<LgUser> users = new HashSet<LgUser>( 0 );
}
