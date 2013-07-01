package models;

import controllers.Secure;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;

@Entity
public class BillDeposit extends LgDeposit {

    public BillDeposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        super(user, userCode, userCodeData);
    }

    public Currency getCurrency() {
        Iterator<LgBill> i = bills.iterator();
        if (i.hasNext()) {
            LgBill b = i.next();
            return b.billType.getCurrency();
        }
        return Currency.findByNumericId(Configuration.getDefaultCurrency());
    }

    public Long getTotal() {
        Long r = BillDeposit.find("select sum(b.quantity * bt.denomination) "
                + " from BillDeposit d, LgBill b, LgBillType bt "
                + " where b.deposit = d and b.billType = bt"
                + " and d.depositId = ?", depositId).first();
        if (r == null) {
            return new Long(0);
        }
        return r;
    }

    public List getDepositContent() {
        List qret = BillDeposit.find(" "
                + "select bt, sum( b.quantity )"
                + " from BillDeposit d, LgBill b, LgBillType bt"
                + " where b.deposit = d "
                + " and b.billType = bt"
                + " and d.depositId = ?"
                + " group by bt.billTypeId, bt.denomination, bt.unitLov, bt.slot, bt.currency, bt.creationDate, bt.endDate"
                + " order by bt.denomination desc"
                + "", this.depositId).fetch();
        return qret;
    }

    public List<BillDAO> getBillList() {
        List<BillDAO> ret = new ArrayList<BillDAO>();
        List qret = getDepositContent();

        for (Object b : qret) {
            Object[] a = (Object[]) b;
            Long quantity = (Long) a[ 1];

            BillDAO bill = new BillDAO((LgBillType) a[0]);
            bill.q = quantity.intValue();
            bill.dq = quantity.intValue();
            ret.add(bill);
        }
        return ret;
    }

    @Override
    public void setRenderArgs(Map args) {
        args.put("clientCode", Configuration.getClientDescription());
        args.put("current_user", Secure.getCurrentUser());
        args.put("providerCode", Configuration.getProviderDescription());
        args.put("branchCode", Configuration.getBranchCode());
        args.put("machineCode", Configuration.getMachineCode());
        List<BillDAO> bl = this.getBillList();
        args.put("billData", bl);
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
}
