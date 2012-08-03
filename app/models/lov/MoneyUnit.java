package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;
import play.Logger;

/**
 *
 * @author adji
 */
@Entity
public class MoneyUnit extends LgLov {

    public static MoneyUnit findByTextId( String textId ) {
        return ( MoneyUnit ) LgLov.findByTextId( MoneyUnit.class.getSimpleName(), textId );
    }

    public static MoneyUnit findByNumericId( Integer numericId ) {
        return ( MoneyUnit ) LgLov.findByNumericId( MoneyUnit.class.getSimpleName(), numericId );
    }
}
