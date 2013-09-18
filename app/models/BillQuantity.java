package models;

import models.lov.Currency;

/**
 * @author adji
 */
public class BillQuantity {
    // Bill type description, denomination + currency

    final public BillValue billValue;
    // Desired Quantity
    public Integer desiredQuantity = 0;
    // Quantity
    public Integer quantity = 0;

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

    public Integer getDenomination() {
        return billValue.denomination;
    }

    public Currency getCurrency() {
        return billValue.currency;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getDesiredQuantity() {
        return desiredQuantity;
    }

    public Integer getAmmount() {
        return quantity * billValue.denomination;
    }
}
