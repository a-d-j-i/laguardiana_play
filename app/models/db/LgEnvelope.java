/*
 I'm assuming that each machine has only one way of taking envelopes, so there is no device <-> envelope relation
 */
package models.db;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import models.lov.EnvelopeType;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_envelope", schema = "public")
public final class LgEnvelope extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "envelope_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgEnvelopeGenerator")
    @SequenceGenerator(name = "LgEnvelopeGenerator", sequenceName = "lg_envelope_sequence")
    public Integer envelopeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", nullable = false)
    public LgDeposit deposit;
    @Column(name = "envelope_type_lov", nullable = false)
    public Integer envelopeTypeLov;
    @Column(name = "envelope_number", nullable = false, length = 128)
    public String envelopeNumber;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "envelope")
    public List<LgEnvelopeContent> envelopeContents = new ArrayList<LgEnvelopeContent>(0);

    public LgEnvelope(Integer envelopeTypeLov, String envelopeNumber) {
        this.envelopeTypeLov = envelopeTypeLov;
        this.envelopeNumber = envelopeNumber;
    }

    public LgEnvelope(LgEnvelope e) {
        this(e.envelopeTypeLov, e.envelopeNumber);
        for (LgEnvelopeContent c : e.envelopeContents) {
            addContent(new LgEnvelopeContent(c));
        }
    }

    public void addContent(LgEnvelopeContent c) {
        c.envelope = this;
        envelopeContents.add(c);
    }

    @Override
    public String toString() {
        String et = null;
        if (envelopeTypeLov != null) {
            EnvelopeType evt = EnvelopeType.findByNumericId(envelopeTypeLov);
            if (evt != null) {
                et = evt.description;
            }
        }
        return "LgEnvelope{" + "envelopeId=" + envelopeId + ", envelopeTypeLov=" + et + ", envelopeNumber=" + envelopeNumber + '}';
    }
}
