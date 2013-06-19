package models;

import controllers.Secure;
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
        return "ReportDepositController/envelopeDepositDetail.html";
    }

    @Override
    public void setRenderArgs(Map args) {
        args.put("clientCode", Configuration.getClientDescription());
        args.put("current_user", Secure.getCurrentUser());
        args.put("providerCode", Configuration.getProviderDescription());
        args.put("branchCode", Configuration.getBranchCode());
        args.put("machineCode", Configuration.getMachineCode());
        args.put("deposit", this);
        args.put("currentDate", new Date());
    }

    @Override
    public void print(boolean reprint) {
        Map args = new HashMap();
        // Print the ticket.
        setRenderArgs(args);
        if (reprint) {
            args.put("reprint", "true");
        }
        ModelFacade.print("PrinterController/envelopeDeposit_finish.html", args, Configuration.getPrintWidth(), Configuration.getEvelopeFinishPrintLen());
    }

    // Merge somehow with print...
    public void printStart() {
        Map args = new HashMap();
        // Print the ticket.
        //List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        //List<Currency> currencies = Currency.findAll();
        setRenderArgs(args);
        ModelFacade.print("PrinterController/envelopeDeposit_start.html", args, Configuration.getPrintWidth(), Configuration.getEvenlopeStartPrintLen());
    }
}
