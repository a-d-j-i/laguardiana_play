package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateFinish extends MachineStateAbstract {

    private final Integer currentUserId;
    private final LgDeposit.FinishCause finishCause;
    private final Integer billDepositId;

    public P500MeiStateFinish(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId, LgDeposit.FinishCause finishCause) {
        super(machine);
        this.currentUserId = currentUserId;
        this.billDepositId = billDepositId;
        this.finishCause = finishCause;
    }

    @Override
    public boolean onStart() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        billDeposit.closeDeposit(finishCause);
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.finish", "FINISH", "billdeposit.finish", currentSum, currentSum);
    }

    @Override
    public boolean onConfirmDepositEvent() {
        return machine.setCurrentState(new P500MeiStateWaiting(machine));
    }
}
