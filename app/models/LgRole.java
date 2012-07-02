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
    int roleId;
    @Column( name = "name", nullable = false, length = 64 )
    String name;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgRole" )
    Set<LgAclRule> lgAclRules = new HashSet<LgAclRule>( 0 );
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgRoles" )
    Set<LgUser> lgUsers = new HashSet<LgUser>( 0 );
}
