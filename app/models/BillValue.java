package models;

import models.lov.Currency;

/**
 *
 * @author adji
 */
public class BillValue implements Comparable {

    final public Currency currency;
    final public Integer denomination;

    public BillValue(Integer unit_lov, Integer denomination) {
        currency = Currency.findByNumericId(unit_lov);
        if (currency == null) {
            throw new RuntimeException("Invalid unit_lov");
        }
        this.denomination = denomination;
    }

    @Override
    public String toString() {
        return currency.numericId + "_" + denomination;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.currency != null ? this.currency.hashCode() : 0);
        hash = 79 * hash + (this.denomination != null ? this.denomination.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BillValue other = (BillValue) obj;
        if (this.currency != other.currency && (this.currency == null || !this.currency.equals(other.currency))) {
            return false;
        }
        if (this.denomination != other.denomination && (this.denomination == null || !this.denomination.equals(other.denomination))) {
            return false;
        }
        return true;
    }

    public int compareTo(Object obj) {
        if (obj == null) {
            return 1;
        }
        if (getClass() != obj.getClass()) {
            return 1;
        }

        final BillValue other = (BillValue) obj;
        final int u = this.currency.description.compareTo(other.currency.description);
        if (u != 0) {
            return u;
        } else {
            return this.denomination.compareTo(other.denomination);
        }
    }
}
