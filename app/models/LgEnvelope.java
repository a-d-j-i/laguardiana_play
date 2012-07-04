package models;

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
    @Column( name = "content_type_lov" )
    public Integer contentTypeLov;
    @Column( name = "amount", nullable = false )
    public int amount;
    @Column( name = "unit_lov", nullable = false )
    public int unitLov;
}
