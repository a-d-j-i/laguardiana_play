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
public class DepositUserCodeReference extends LgLov {

    public static DepositUserCodeReference findByTextId(String textId) {
        return (DepositUserCodeReference) LgLov.findByTextId(DepositUserCodeReference.class.getSimpleName(), textId);
    }

    public static DepositUserCodeReference findByNumericId(Integer numericId) {
        return (DepositUserCodeReference) LgLov.findByNumericId(DepositUserCodeReference.class.getSimpleName(), numericId);
    }

    public static <T extends JPABase> List<T> findEnabled() {
        return LgLov.findEnabled(DepositUserCodeReference.class.getSimpleName());
    }

}
