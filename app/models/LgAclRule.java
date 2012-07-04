package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_acl_rule", schema = "public" )
public class LgAclRule extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "acl_id", unique = true, nullable = false )
    @GeneratedValue
    public int aclId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "role_id", nullable = false )
    public LgRole lgRole;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "resource_id", nullable = false )
    public LgResource lgResource;
    @Column( name = "priority", nullable = false )
    public int priority;
    @Column( name = "permission", nullable = false, length = 8 )
    public String permission;
    @Column( name = "operation", nullable = false, length = 64 )
    public String operation;
}
