package models;

import models.lov.Currency;

/**
 * @author adji
 */
public class BillQuantity {
    // Bill type description, denomination + currency

    final public BillValue billValue;
    // Desired Quantity
    public Long desiredQuantity = 0l;
    // Quantity
    public Long quantity = 0l;

    public BillQuantity(BillValue value) {
        this.billValue = value;
    }

    @Override
    public String toString() {
        return "BillQuantity{" + "billValue=" + billValue + ", dq=" + desiredQuantity + ", q=" + quantity + '}';
    }

    public String getKey() {
        return billValue.currency.numericId + "_" + billValue.denomination;
    }

    public Long getDenomination() {
        return billValue.denomination;
    }

    public Currency getCurrency() {
        return billValue.currency;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getDesiredQuantity() {
        return desiredQuantity;
    }

    public Long getAmmount() {
        return quantity * billValue.denomination;
    }
}
