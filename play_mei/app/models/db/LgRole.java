package models.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_role", schema = "public")
public class LgRole extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "role_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgRoleGenerator")
    @SequenceGenerator(name = "LgRoleGenerator", sequenceName = "lg_role_sequence")
    public Integer roleId;
    @Column( name = "name", nullable = false, length = 64)
    public String name;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "role")
    @OrderBy("priority")
    public List<LgAclRule> aclRules = new ArrayList<LgAclRule>(0);
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "roles")
    public Set<LgUser> users = new HashSet<LgUser>(0);

    @Override
    public String toString() {
        return "LgRole{" + "roleId=" + roleId + ", name=" + name + '}';
    }
}
