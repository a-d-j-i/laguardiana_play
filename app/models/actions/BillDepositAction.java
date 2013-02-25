/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.ManagerInterface;
import java.util.Date;
import java.util.EnumMap;
import models.BillDeposit;
import models.actions.states.BillDepositStart;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    static final EnumMap<ManagerInterface.MANAGER_STATE, String> messageMap = new EnumMap<ManagerInterface.MANAGER_STATE, String>(ManagerInterface.MANAGER_STATE.class);

    static {
        messageMap.put(ManagerInterface.MANAGER_STATE.READY_TO_STORE, "bill_deposit.ready_to_store");
        messageMap.put(ManagerInterface.MANAGER_STATE.ESCROW_FULL, "bill_deposit.escrow_full");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(currency, formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        state = new BillDepositStart(new StateApi());
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    @Override
    public void start() {
        BillDeposit deposit = new BillDeposit(currentUser, userCode, userCodeLov);
        deposit.startDate = new Date();
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.count(currency.numericId);
    }

    @Override
    public void finish() {
        BillDeposit deposit = BillDeposit.findById(getDepositId());
        if (deposit != null && deposit.getTotal() > 0) {
            deposit.print(userActionApi.getPrinter(), false);
        }
    }
}
