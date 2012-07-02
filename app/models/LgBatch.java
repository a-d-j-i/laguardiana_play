package models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_batch", schema = "public" )
public class LgBatch extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "batch_id", unique = true, nullable = false )
    int batchId;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    Date creationDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lgBatch" )
    Set<LgBill> lgBills = new HashSet<LgBill>( 0 );
}
