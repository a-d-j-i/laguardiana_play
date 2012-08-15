package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
public class EnvelopeContentType extends LgLov {

    public static EnvelopeContentType findByTextId( String textId ) {
        return ( EnvelopeContentType ) LgLov.findByTextId( EnvelopeContentType.class.getSimpleName(), textId );
    }

    public static EnvelopeContentType findByNumericId( Integer numericId ) {
        return ( EnvelopeContentType ) LgLov.findByNumericId( EnvelopeContentType.class.getSimpleName(), numericId );
    }
}
