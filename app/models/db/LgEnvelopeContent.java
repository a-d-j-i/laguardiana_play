package models.db;

import javax.persistence.*;
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

        private EnvelopeContentType(Integer lov) {
            this.lov = lov;
        }

        public Integer getLov() {
            return lov;
        }
    }
    @Id
    @Column(name = "envelope_content_id", unique = true, nullable = false)
    @GeneratedValue
    public int envelopeContentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    public LgEnvelope envelope;
    @Column(name = "content_type_lov")
    public Integer contentTypeLov;
    @Column(name = "amount", nullable = true)
    public Integer amount;
    @Column(name = "unit_lov", nullable = true)
    public Integer unitLov;

    public LgEnvelopeContent(EnvelopeContentType contentTypeLov, Integer amount, Integer unitLov) {
        this.contentTypeLov = contentTypeLov.getLov();
        this.amount = amount;
        this.unitLov = unitLov;
    }
}
