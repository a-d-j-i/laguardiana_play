package models.lov;

import javax.persistence.Entity;
import models.db.LgLov;

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
