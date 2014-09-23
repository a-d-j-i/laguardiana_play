package models.lov;

import java.util.List;
import javax.persistence.Entity;
import models.db.LgLov;
import play.db.jpa.JPABase;

/**
 *
 * @author adji
 */
@Entity
public class EnvelopeType extends LgLov {

    public static EnvelopeType findByTextId(String textId) {
        return (EnvelopeType) LgLov.findByTextId(EnvelopeType.class.getSimpleName(), textId);
    }

    public static EnvelopeType findByNumericId(Integer numericId) {
        return (EnvelopeType) LgLov.findByNumericId(EnvelopeType.class.getSimpleName(), numericId);
    }

    public static <T extends JPABase> List<T> findEnabled() {
        return LgLov.findEnabled(DepositUserCodeReference.class.getSimpleName());
    }
}
