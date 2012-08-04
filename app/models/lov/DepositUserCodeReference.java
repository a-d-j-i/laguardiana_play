package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
public class DepositUserCodeReference extends LgLov {

    public static DepositUserCodeReference findByTextId( String textId ) {
        return ( DepositUserCodeReference ) LgLov.findByTextId( DepositUserCodeReference.class.getSimpleName(), textId );
    }

    public static DepositUserCodeReference findByNumericId( Integer numericId ) {
        return ( DepositUserCodeReference ) LgLov.findByNumericId( DepositUserCodeReference.class.getSimpleName(), numericId );
    }
}
