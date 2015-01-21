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
public class Currency extends LgLov implements Comparable {

    public static Currency findByTextId(String textId) {
        return (Currency) LgLov.findByTextId(Currency.class.getSimpleName(), textId);
    }

    public static Currency findByNumericId(Integer numericId) {
        return (Currency) LgLov.findByNumericId(Currency.class.getSimpleName(), numericId);
    }

    public static <T extends JPABase> List<T> findEnabled() {
        return LgLov.findEnabled(Currency.class.getSimpleName());
    }

    public int compareTo(Object obj) {
        if (obj == null) {
            return 1;
        }
        if (getClass() != obj.getClass()) {
            return 1;
        }

        final Currency other = (Currency) obj;
        return this.description.compareTo(other.textId);
    }
}
