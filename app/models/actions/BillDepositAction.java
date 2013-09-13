/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import java.util.Date;
import models.BillDeposit;
import models.actions.states.BillDepositStart;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    public DepositUserCodeReference userCodeLov;
    public String userCode;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(currency, formData);
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
        if (deposit != null) {
            if (deposit.getTotal() > 0) {
                deposit.print(false);
            }
            deposit.finishDate = new Date();
            deposit.save();
        }
    }
}
