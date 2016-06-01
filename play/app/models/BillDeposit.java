package models;

import controllers.Secure;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgUser;
import models.lov.Currency;
import play.Logger;

@Entity
public class BillDeposit extends LgDeposit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency", nullable = true)
    public Currency currency;

    public BillDeposit(LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        super(user, userCode, userCodeLovId);
        this.currency = currency;
    }

    public BillDeposit(BillDeposit refDeposit) {
        this((LgUser) LgUser.findById(refDeposit.user.userId), (Currency) Currency.findById(refDeposit.currency.lovId),
                refDeposit.userCode, refDeposit.userCodeLov);
    }

    public List getDepositContent() {
        List qret = BillDeposit.find(" "
                + "select bt, sum( b.quantity )"
                + " from BillDeposit d, LgBill b, LgBillType bt"
                + " where b.deposit = d "
                + " and b.billType = bt"
                + " and d.depositId = ?"
                + " group by bt"
                + " order by bt.denomination desc"
                + "", this.depositId).fetch();
        return qret;
    }

    public List<BillQuantity> getBillList() {
        List<BillQuantity> ret = new ArrayList<BillQuantity>();
        List qret = BillDeposit.find(" "
                + "select bt, sum( b.quantity )"
                + " from BillDeposit d, LgBill b, LgBillType bt"
                + " where b.deposit = d "
                + " and b.billType = bt"
                + " and bt.endDate is null"
                + " and d.depositId = ?"
                + " group by bt"
                + " order by bt.denomination desc"
                + "", this.depositId).fetch();

        for (Object b : qret) {
            Object[] a = (Object[]) b;
            Long quantity = (Long) a[2];
<<<<<<< HEAD:play/app/models/BillDeposit.java
            BillValue bv = new BillValue((Currency) a[1], (Integer) a[0]);
=======
            BillValue bv = new BillValue((Integer) a[1], (Integer) a[0]);
>>>>>>> 5b6aebaccd5ff8e589943295d3e6f39d9c74b253:app/models/BillDeposit.java
            BillQuantity bill = new BillQuantity(bv);
            bill.quantity = quantity.intValue();
            ret.add(bill);
        }
        return ret;
    }

    @Override
    public void setRenderArgs(Map args) {
        args.put("showReference1", Configuration.mustShowBillDepositReference1());
        args.put("showReference2", Configuration.mustShowBillDepositReference2());
        args.put("clientCode", Configuration.getClientDescription());
        args.put("current_user", Secure.getCurrentUser());
        args.put("providerCode", Configuration.getProviderDescription());
        args.put("branchCode", Configuration.getBranchCode());
        args.put("machineCode", Configuration.getMachineCode());
        args.put("ticketFooter", Configuration.getTicketFooter());
        args.put("ticketHeader", Configuration.getTicketHeader());
        ReportTotals totals = new ReportTotals();
        args.put("billData", totals.visitBillDeposit(this));
        args.put("depositTotal", this.getTotal());
        args.put("deposit", this);
        args.put("currentDate", new Date());
    }

    @Override
    public String getDetailView() {
        return "ReportDepositController/billDepositDetail.html";
    }

    @Override
    public void print(boolean reprint) {
        Map args = new HashMap();
        // Print the ticket.
        setRenderArgs(args);
        if (reprint) {
            args.put("reprint", "true");
        }
        ModelFacade.print("PrinterController/billDeposit.html", args, Configuration.getPrintWidth(), Configuration.getBillDepositPrintLen());
    }

    // TODO: By currency
    public Long getTotal() {
        Long ret = 0L;
        for (LgBill b : bills) {
            LgBillType bt = b.billType;
            if (bt.currency != currency) {
                Logger.error("NEED TO SUPPORT MULTIPLE CURRENCY %s != %s", bt.currency, currency);
            } else {
                ret += (b.quantity * bt.denomination);
            }
        }
        return ret;
    }

    public boolean addBillToDeposit(LgBatch batch, LgBillType billType, int quantity) {
        Logger.debug("Adding to deposit %d, batch %d, billType %s, quantity %d", depositId, batch.batchId, billType.toString(), quantity);
        LgBill b = LgBill.getOrCreate(this, batch, billType);
        b.quantity += quantity;
        b.save();
        return true;
    }

    public Map<LgBillType, Integer> getCurrentQuantity() {
        Map<LgBillType, Integer> ret = new HashMap<LgBillType, Integer>();
        for (LgBill b : bills) {
            LgBillType bt = b.billType;
            Integer c = ret.containsKey(bt) ? ret.get(bt) : 0;
            c += b.quantity;
            ret.put(bt, c);
        }
        return ret;
    }

}
