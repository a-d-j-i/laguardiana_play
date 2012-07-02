package models;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_resource", schema = "public" )
public class LgResource extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "resource_id", unique = true, nullable = false )
    int resourceId;
    @Column( name = "name", nullable = false, length = 64 )
    String name;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgResource" )
    Set<LgAclRule> lgAclRules = new HashSet<LgAclRule>( 0 );
}
