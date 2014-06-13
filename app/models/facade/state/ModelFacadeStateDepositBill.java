package models.facade.state;

import models.facade.state.substate.ModelFacadeSubStateApi;
import models.ModelFacade;
import controllers.BillDepositController;
import java.util.Date;
import models.BillDeposit;
import models.facade.state.substate.bill_deposit.BillDepositStart;
import models.facade.status.ModelFacadeStateStatus;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class ModelFacadeStateDepositBill extends ModelFacadeStateAbstract {

    private final BillDepositController.BillDepositData data;
    protected Integer currentDepositId = null;

    public ModelFacadeStateDepositBill(ModelFacade.ModelFacadeStateApi api, BillDepositController.BillDepositData data) {
        super(api);
        this.data = data;
    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("BillDepositController", subState, data, currentDepositId);
    }

    @Override
    public ModelFacadeStateAbstract init() {
        subState = new BillDepositStart(new ModelFacadeSubStateApi(api));
        BillDeposit deposit = new BillDeposit(data.currentUser, data.reference2, (DepositUserCodeReference) data.reference1.lov);
        deposit.startDate = new Date();
        deposit.save();
        currentDepositId = deposit.depositId;
        api.getMachine().count(data.currency.currency.numericId, null);
        return this;
    }

    @Override
    public boolean finish() {
        if (!subState.canFinishAction()) {
            return false;
        }
        BillDeposit deposit = BillDeposit.findById(currentDepositId);
        if (deposit != null) {
            deposit.finishDate = new Date();
            deposit.save();
            if (deposit.getTotal() > 0) {
                deposit.print(false);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ModelFacadeStateBillDeposit";
    }

}
