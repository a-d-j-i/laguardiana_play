package models;

import devices.DeviceFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import models.db.LgDeposit;
import models.db.LgUser;
import models.lov.DepositUserCodeReference;

@Entity
public class EnvelopeDeposit extends LgDeposit {

    public EnvelopeDeposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        super(user, userCode, userCodeData);
    }

    @Override
    public String getDetailView() {
        return "ReportController/envelopeDeposit_finish.html";
    }

    @Override
    public void setRenderArgs(Map args) {
        args.put("clientCode", Configuration.getClientDescription());
        args.put("providerCode", Configuration.getProviderDescription());
        args.put("branchCode", Configuration.getBranchCode());
        args.put("machineCode", Configuration.getMachineCode());
        args.put("deposit", this);
        args.put("currentDate", new Date());
    }

    @Override
    public void print() {
        Map args = new HashMap();
        // Print the ticket.
        setRenderArgs(args);
        DeviceFactory.getPrinter().print("PrinterController/envelopeDeposit_finish.html", args, 300);
    }

    // Merge somehow with print...
    public void printStart() {
        Map args = new HashMap();
        // Print the ticket.
        //List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        //List<Currency> currencies = Currency.findAll();
        setRenderArgs(args);
        DeviceFactory.getPrinter().print("PrinterController/envelopeDeposit_start.html", args, 300);
    }
}
