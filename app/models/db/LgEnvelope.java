package models.db;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_envelope", schema = "public" )
public class LgEnvelope extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "envelope_id", unique = true, nullable = false )
    @GeneratedValue
    public int envelopeId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id", nullable = false )
    public LgDeposit deposit;
    @Column( name = "envelope_type_lov", nullable = false )
    public int envelopeTypeLov;
    @Column( name = "envelope_number", nullable = false, length = 128 )
    public String envelopeNumber;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "envelope" )
    public Set<LgEnvelopeContent> envelopeContents = new HashSet<LgEnvelopeContent>( 0 );
}
