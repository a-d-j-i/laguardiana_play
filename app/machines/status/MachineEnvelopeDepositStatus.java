package machines.status;

import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
public class MachineEnvelopeDepositStatus extends MachineStatus {

    final Integer depositId;

    public MachineEnvelopeDepositStatus(Integer depositId, Integer currentUserId, String neededAction, String stateName) {
        super(currentUserId, neededAction, stateName);
        this.depositId = depositId;
    }

    public EnvelopeDeposit getCurrentDeposit() {
        return EnvelopeDeposit.findById(depositId);
    }

    @Override
    public String toString() {
        return "MachineEnvelopeDepositStatus{" + "depositId=" + depositId + '}';
    }

}
