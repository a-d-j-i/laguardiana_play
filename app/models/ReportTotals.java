/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import models.db.LgBill;
import models.db.LgDeposit;
import models.db.LgDeposit.DepositVisitor;
import models.lov.Currency;

/**
 *
 * @author adji
 */
public class ReportTotals implements DepositVisitor {

    public static class Total {

        public long ammount = 0;
        public long quantity = 0;
        private SortedMap<BillValue, BillQuantity> detail = new TreeMap<BillValue, BillQuantity>();

        public Collection<BillQuantity> getDetail() {
            return detail.values();
        }
    }
    public long envelopes = 0;
    public long deposits = 0;
    public long bills = 0;
    private Map<Currency, Total> byCurrencyTotal = new HashMap<Currency, Total>();

    public Total getCurrencyTotal(Currency c) {
        return byCurrencyTotal.get(c);
    }

    public Collection<BillQuantity> getDetail(Currency c) {
        Total t = byCurrencyTotal.get(c);
        return t.getDetail();
    }

    public Set<Currency> getCurrencies() {
        return byCurrencyTotal.keySet();
    }

    public ReportTotals visitBillDeposit(BillDeposit item) {
        deposits++;
        for (LgBill b : item.bills) {
            bills += b.quantity;
            Currency c = b.billType.getCurrency();

            Total at = byCurrencyTotal.get(c);
            if (at == null) {
                at = new Total();
                byCurrencyTotal.put(c, at);
            }
            at.ammount += b.getTotal();
            at.quantity += b.quantity;

            SortedMap<BillValue, BillQuantity> ct = at.detail;
            if (ct == null) {
                ct = new TreeMap<BillValue, BillQuantity>();
            }
            BillQuantity bill = ct.get(b.billType.getValue());
            if (bill == null) {
                bill = new BillQuantity(b.billType.getValue());
                ct.put(b.billType.getValue(), bill);
            }
            bill.quantity += b.quantity;
            at.detail = ct;
        }
        return this;
    }

    public ReportTotals visitEnvelopeDeposit(EnvelopeDeposit item) {
        deposits++;
        envelopes++;
        return this;
    }

    public void visit(LgDeposit item) {
        if (item instanceof EnvelopeDeposit) {
            visitEnvelopeDeposit((EnvelopeDeposit) item);
        } else if (item instanceof BillDeposit) {
            visitBillDeposit((BillDeposit) item);
        } else {
            throw new RuntimeException(String.format("Invalid deposit type %s", item.getClass()));
        }
    }
    /*    public void visit(BillDeposit item) {
     Logger.error("Invalid deposit type");
     throw new UnsupportedOperationException("Not supported yet.");
     }*/

    synchronized public ReportTotals getTotals(Set<LgDeposit> deps) {
        LgDeposit.visitDeposits(deps, this);
        return this;
    }
}
