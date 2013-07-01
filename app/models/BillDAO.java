package models;

import models.db.LgBillType;
import models.lov.Currency;

/**
 * @author adji
 */
public class BillDAO {

    transient final public LgBillType billType;
    // Bill type id
    public Integer tid;
    // Bill type description
    public String btd;
    // Denomination
    public Integer d;
    // Desired Quantity
    public Integer dq = 0;
    // Quantity
    public Integer q = 0;
    // Currency
    public String c = "";

    public BillDAO(LgBillType bb) {
        this.billType = bb;
        this.tid = bb.billTypeId;
        this.btd = bb.toString();
        this.d = bb.denomination;
        Currency currency = bb.getCurrency();
        if (currency == null) {
            this.c = "-";
        } else {
            this.c = currency.textId;
        }
    }

    @Override
    public String toString() {
        return "Bill{" + "billType=" + billType + ", tid=" + tid + ", btd=" + btd + ", d=" + d + ", dq=" + dq + ", q=" + q + '}';
    }
}
