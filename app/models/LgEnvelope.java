package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_envelope", schema = "public" )
public class LgEnvelope extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "envelope_id", unique = true, nullable = false )
    int envelopeId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id", nullable = false )
    LgDeposit lgDeposit;
    @Column( name = "envelope_type_lov", nullable = false )
    int envelopeTypeLov;
    @Column( name = "envelope_number", nullable = false, length = 128 )
    String envelopeNumber;
    @Column( name = "content_type_lov" )
    Integer contentTypeLov;
    @Column( name = "amount", nullable = false )
    int amount;
    @Column( name = "unit_lov", nullable = false )
    int unitLov;
}
