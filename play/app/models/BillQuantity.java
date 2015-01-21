package models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import models.db.LgBillType;
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

    // Helper
    private interface BillListVisitor {

        public void visit(LgBillType billType, Integer desired, Integer current);
    }

    public static Collection<BillQuantity> getBillQuantities(Currency currency, Map<LgBillType, Integer> currentQuantity, Map<LgBillType, Integer> desiredQuantity) {
        final SortedMap<BillValue, BillQuantity> ret = new TreeMap<BillValue, BillQuantity>();
        visitBillList(currentQuantity, desiredQuantity, currency, new BillListVisitor() {
            public void visit(LgBillType billType, Integer desired, Integer current) {
                BillValue bv = billType.getValue();
                BillQuantity billQuantity = ret.get(bv);
                if (billQuantity == null) {
                    billQuantity = new BillQuantity(bv);
                }
                billQuantity.quantity += current;
                billQuantity.desiredQuantity += desired;
                ret.put(bv, billQuantity);
            }
        });
        return ret.values();
    }

    private static void visitBillList(Map<LgBillType, Integer> currentQuantity, Map<LgBillType, Integer> desiredQuantity, Currency currency, BillListVisitor visitor) {
        // Union of all the bill types for current currency and for the data received.1
        Set<LgBillType> billTypes = new HashSet<LgBillType>();
        if (currentQuantity != null) {
            billTypes.addAll(currentQuantity.keySet());
        }
        if (desiredQuantity != null) {
            billTypes.addAll(desiredQuantity.keySet());
        }
        billTypes.addAll(LgBillType.find(currency));

        // Sum over all bill types ignoring the device and slot they came from.
        // Plan B: separate the slots by device.
        for (LgBillType billType : billTypes) {
            Integer desired = 0;
            Integer current = 0;
            if (currentQuantity != null && currentQuantity.containsKey(billType)) {
                current = currentQuantity.get(billType);
            }
            if (desiredQuantity != null && desiredQuantity.containsKey(billType)) {
                desired = desiredQuantity.get(billType);
            }
            visitor.visit(billType, desired, current);
        }
    }

}
