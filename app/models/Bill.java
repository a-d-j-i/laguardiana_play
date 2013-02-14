/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.DeviceFactory;
import devices.glory.manager.ManagerInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.db.LgBillType;
import models.lov.Currency;

/**
 * @author adji
 * This class must be splitted between controller and model in a happier way.
 */
public class Bill {

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

    public Bill(LgBillType bb) {
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
    // TODO: Refactor into controller and model parts.
    static public List<Bill> getBillList(Integer currency) {
        List<Bill> ret = new ArrayList<Bill>();
        List<LgBillType> billTypes = LgBillType.find(currency);


        Map<Integer, Integer> desiredQuantity = null;
        Map<Integer, Integer> currentQuantity = null;
        ManagerInterface manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            currentQuantity = manager.getCurrentQuantity();
            desiredQuantity = manager.getDesiredQuantity();
        }
        for (LgBillType bb : billTypes) {
            Bill b = new Bill(bb);
            if (currentQuantity != null && currentQuantity.containsKey(bb.slot)) {
                b.q = currentQuantity.get(bb.slot);
            }
            if (desiredQuantity != null && desiredQuantity.containsKey(bb.slot)) {
                b.dq = desiredQuantity.get(bb.slot);
            }
            ret.add(b);
        }
        return ret;
    }

    static public List getDepositContent(BillDeposit deposit) {
        List qret = BillDeposit.find(" "
                + "select bt, sum( b.quantity )"
                + " from BillDeposit d, LgBill b, LgBillType bt"
                + " where b.deposit = d "
                + " and b.billType = bt"
                + " and d.depositId = ?"
                + " group by bt.billTypeId, bt.denomination, bt.unitLov, bt.slot, bt.currency, bt.creationDate, bt.endDate"
                + " order by bt.denomination desc"
                + "", deposit.depositId).fetch();
        return qret;
    }

    static public List<Bill> getDepositBillList(BillDeposit deposit) {
        List<Bill> ret = new ArrayList<Bill>();
        List qret = getDepositContent(deposit);

        Map<Integer, Integer> desiredQuantity = null;
        Map<Integer, Integer> currentQuantity = null;
        ManagerInterface manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            currentQuantity = manager.getCurrentQuantity();
            desiredQuantity = manager.getDesiredQuantity();
        }
        for (Object b : qret) {
            Object[] a = (Object[]) b;
            Long quantity = (Long) a[ 1];

            Bill bill = new Bill((LgBillType) a[0]);
            bill.q = quantity.intValue();
            bill.dq = quantity.intValue();
            ret.add(bill);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Bill{" + "billType=" + billType + ", tid=" + tid + ", btd=" + btd + ", d=" + d + ", dq=" + dq + ", q=" + q + '}';
    }
}
