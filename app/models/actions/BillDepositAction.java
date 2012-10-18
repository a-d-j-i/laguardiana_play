/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.DeviceFactory;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import models.BillDeposit;
import models.actions.states.IdleBillDeposit;
import models.db.LgSystemProperty;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    static final EnumMap<GloryManager.State, String> messageMap = new EnumMap<GloryManager.State, String>(GloryManager.State.class);

    static {
        messageMap.put(GloryManager.State.READY_TO_STORE, "bill_deposit.ready_to_store");
        messageMap.put(GloryManager.State.ESCROW_FULL, "bill_deposit.escrow_full");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(currency, formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        state = new IdleBillDeposit(new StateApi(), false);
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    // TODO: Move to states
    @Override
    public void start() {
        BillDeposit deposit = new BillDeposit(currentUser, userCode, userCodeLov);
        deposit.startDate = new Date();
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.count(currency.numericId);
    }

    // TODO: Move to states
    @Override
    public void finish() {
        Map renderArgs = new HashMap();
        renderArgs.put("clientCode", LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE));
        renderArgs.put("formData", formData);
        BillDeposit deposit = BillDeposit.findById(getDepositId());
        if (deposit != null && deposit.getTotal() > 0) {
            renderArgs.put("depositTotal", deposit.getTotal());
            renderArgs.put("depositId", deposit.depositId);
            try {
                // Print the ticket.
                DeviceFactory.getPrinter().print("billDeposit", renderArgs);
            } catch (Throwable ex) {
                Logger.debug(ex.getMessage());
            }
        }
    }
}
