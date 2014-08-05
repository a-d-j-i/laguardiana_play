package machines.status;

import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
public class MachineEnvelopeDepositStatus extends MachineStatus {

    public MachineEnvelopeDepositStatus(Integer currentUserId, String neededController, String neededAction, String stateName) {
        super(currentUserId, neededAction, stateName);
    }

    public EnvelopeDeposit getCurrentDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
