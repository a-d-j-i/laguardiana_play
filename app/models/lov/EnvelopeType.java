package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
public class EnvelopeType extends LgLov {

    public static EnvelopeType findByTextId( String textId ) {
        return ( EnvelopeType ) LgLov.findByTextId( EnvelopeType.class.getSimpleName(), textId );
    }

    public static EnvelopeType findByNumericId( Integer numericId ) {
        return ( EnvelopeType ) LgLov.findByNumericId( EnvelopeType.class.getSimpleName(), numericId );
    }
}
