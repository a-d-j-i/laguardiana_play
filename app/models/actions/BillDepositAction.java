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
import models.Deposit;
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
        messageMap.put(GloryManager.State.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.State.ESCROW_FULL, "bill_deposit.escrow_full");
        messageMap.put(GloryManager.State.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(GloryManager.State.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(GloryManager.State.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(GloryManager.State.CANCELING, "counting_page.canceling");
        messageMap.put(GloryManager.State.CANCELED, "counting_page.deposit_canceled");
        messageMap.put(GloryManager.State.ERROR, "application.error");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(currency, formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        state = new IdleBillDeposit(new StateApi());
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    // TODO: Move to states
    @Override
    public void start() {
        Deposit deposit = new Deposit(currentUser, userCode, userCodeLov);
        deposit.startDate = new Date();
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.count(currency.numericId);
    }

    // TODO: Move to states
    @Override
    public void finish() {
        Map renderArgs = new HashMap();
        renderArgs.put("clientCode", LgSystemProperty.getProperty("client_code"));
        renderArgs.put("formData", formData);
        Deposit deposit = Deposit.findById(getDepositId());
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
