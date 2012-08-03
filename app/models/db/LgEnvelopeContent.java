package models.db;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_envelope_content", schema = "public" )
public class LgEnvelopeContent extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "envelope_content_id", unique = true, nullable = false )
    @GeneratedValue
    public int envelopeContentId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "envelope_id", nullable = false )
    public LgEnvelope envelope;
    @Column( name = "content_type_lov" )
    public Integer contentTypeLov;
    @Column( name = "amount", nullable = false )
    public int amount;
    @Column( name = "unit_lov", nullable = false )
    public int unitLov;
}
