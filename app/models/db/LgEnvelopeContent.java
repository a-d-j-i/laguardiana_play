package models.db;

import java.util.HashMap;
import javax.persistence.*;
import models.Configuration;
import models.lov.Currency;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_envelope_content", schema = "public")
public class LgEnvelopeContent extends GenericModel implements java.io.Serializable {

    static public enum EnvelopeContentType {

        CASH(1),
        CHECKS(2),
        TICKETS(3),
        DOCUMENTS(4),
        OTHERS(5),;
        final private Integer lov;
        final private static HashMap<Integer, EnvelopeContentType> reverse = new HashMap<Integer, EnvelopeContentType>();

        static {
            for (EnvelopeContentType e : EnvelopeContentType.values()) {
                reverse.put(e.getLov(), e);
            }
        }

        private EnvelopeContentType(Integer lov) {
            this.lov = lov;
        }

        static public EnvelopeContentType find(Integer lov) {
            if (reverse.containsKey(lov)) {
                return reverse.get(lov);
            }
            return null;
        }

        public Integer getLov() {
            return lov;
        }
    }
    @Id
    @Column(name = "envelope_content_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgEnvelopeContentGenerator")
    @SequenceGenerator(name = "LgEnvelopeContentGenerator", sequenceName = "lg_envelope_content_sequence")
    public Integer envelopeContentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    public LgEnvelope envelope;
    @Column(name = "content_type_lov")
    public Integer contentTypeLov;
    @Column(name = "amount", nullable = true)
    public Double amount;
    @Column(name = "unit_lov", nullable = true)
    public Integer unitLov;

    public LgEnvelopeContent(EnvelopeContentType contentTypeLov, Double amount, Integer unitLov) {
        this.contentTypeLov = contentTypeLov.getLov();
        this.amount = amount;
        this.unitLov = unitLov;
    }

    public EnvelopeContentType getType() {
        return EnvelopeContentType.find(contentTypeLov);
    }

    public Currency getCurrency() {
        return Currency.findByNumericId(unitLov);
    }

    @Override
    public String toString() {
        String ct = null;
        if (EnvelopeContentType.find(contentTypeLov) != null) {
            ct = EnvelopeContentType.find(contentTypeLov).name();
        }
        return "envelopeContentId=" + envelopeContentId + ", envelope=" + envelope.envelopeId + ", contentTypeLov=" + ct + ", amount=" + amount + ", unitLov=" + unitLov;
    }
}
