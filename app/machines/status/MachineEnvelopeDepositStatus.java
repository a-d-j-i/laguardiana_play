package machines.status;

import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
public class MachineEnvelopeDepositStatus extends MachineStatus {

    final EnvelopeDeposit deposit;

    public MachineEnvelopeDepositStatus(EnvelopeDeposit deposit, Integer currentUserId, String neededAction, String stateName) {
        super(currentUserId, neededAction, stateName);
        this.deposit = deposit;
    }

    public EnvelopeDeposit getCurrentDeposit() {
        return deposit;
    }

}
