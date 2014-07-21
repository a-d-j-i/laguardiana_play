package models.lov;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
public class Currency extends LgLov implements Comparable {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "currency")
    public Set<LgBillType> billTypes = new HashSet<LgBillType>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "currency")
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>(0);

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
