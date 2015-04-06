/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgDeposit.DepositVisitor;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.lov.Currency;

/**
 *
 * @author adji
 */
public class ReportTotals {

    public static class Total {

        public long validatedAmmount = 0;
        public long validatedQuantity = 0;
        public double cashToValidate = 0;
        public double checksToValidate = 0;
        public double ticketsToValidate = 0;
        private SortedMap<BillValue, BillQuantity> detail = new TreeMap<BillValue, BillQuantity>();

        private Total(Currency c) {
            for (LgBillType bt : LgBillType.find(c)) {
                detail.put(bt.getValue(), new BillQuantity(bt.getValue()));
            }
        }

        public Collection<BillQuantity> getDetail() {
            return detail.values();
        }

        public double getToValidateTotal() {
            return cashToValidate + checksToValidate + ticketsToValidate;
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
            Currency c = b.billType.currency;

            Total at = byCurrencyTotal.get(c);
            if (at == null) {
                at = new Total(c);
                byCurrencyTotal.put(c, at);
            }
            at.validatedAmmount += b.getTotal();
            at.validatedQuantity += b.quantity;

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
        for (LgEnvelope e : item.envelopes) {
            for (LgEnvelopeContent ec : e.envelopeContents) {
                switch (ec.getType()) {
                    case CASH:
                    case CHECKS:
                    case TICKETS:
                        break;
                    case DOCUMENTS:
                    case OTHERS:
                        continue;
                }
                Currency c = ec.getCurrency();
                Total at = byCurrencyTotal.get(c);
                if (at == null) {
                    at = new Total(c);
                    byCurrencyTotal.put(c, at);
                }
                switch (ec.getType()) {
                    case CASH:
                        at.cashToValidate += ec.amount;
                        break;
                    case CHECKS:
                        at.checksToValidate += ec.amount;
                        break;
                    case TICKETS:
                        at.ticketsToValidate += ec.amount;
                        break;
                }
            }
        }
        return this;
    }

    synchronized public ReportTotals getTotals(Set<LgDeposit> deps) {
        LgDeposit.visitDeposits(deps, new DepositVisitor() {

            public void visit(LgDeposit item) {
                if (item.finishDate == null) {
                    return;
                }
                if (item instanceof EnvelopeDeposit) {
                    visitEnvelopeDeposit((EnvelopeDeposit) item);
                } else if (item instanceof BillDeposit) {
                    visitBillDeposit((BillDeposit) item);
                } else {
                    throw new RuntimeException(String.format("Invalid deposit type %s", item.getClass()));
                }
            }
        });
        return this;
    }
}
