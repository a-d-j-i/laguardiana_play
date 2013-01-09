package models;

import devices.DeviceFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import models.db.LgBill;
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

    public List<Bill> getBillList() {
        return Bill.getDepositBillList(this);
    }

    @Override
    public void setRenderArgs(Map args) {
        args.put("clientCode", Configuration.getClientDescription());
        args.put("providerCode", Configuration.getProviderDescription());
        args.put("branchCode", Configuration.getBranchCode());
        args.put("machineCode", Configuration.getMachineCode());
        List<Bill> bl = this.getBillList();
        args.put("billData", bl);
        args.put("depositTotal", this.getTotal());
        args.put("deposit", this);
        args.put("currentDate", new Date());
    }

    @Override
    public String getDetailView() {
        return "ReportController/billDeposit.html";
    }

    @Override
    public void print() {
        Map args = new HashMap();
        // Print the ticket.
        setRenderArgs(args);
        DeviceFactory.getPrinter().print("PrinterController/billDeposit.html", args, 300);
    }
}
