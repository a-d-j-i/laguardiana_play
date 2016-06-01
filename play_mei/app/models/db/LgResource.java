package models.db;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_resource", schema = "public")
public class LgResource extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "resource_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgResourceGenerator")
    @SequenceGenerator(name = "LgResourceGenerator", sequenceName = "lg_resource_sequence")
    public Integer resourceId;
    @Column( name = "name", nullable = false, length = 64)
    public String name;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "resource")
    public Set<LgAclRule> aclRules = new HashSet<LgAclRule>(0);

    @Override
    public String toString() {
        return "LgResource{" + "resourceId=" + resourceId + ", name=" + name + '}';
    }
}
