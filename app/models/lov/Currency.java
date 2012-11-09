package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
public class Currency extends LgLov {
    
    public static Currency findByTextId( String textId ) {
        return ( Currency ) LgLov.findByTextId( Currency.class.getSimpleName(), textId );
    }

    public static Currency findByNumericId( Integer numericId ) {
        return ( Currency ) LgLov.findByNumericId( Currency.class.getSimpleName(), numericId );
    }

}
