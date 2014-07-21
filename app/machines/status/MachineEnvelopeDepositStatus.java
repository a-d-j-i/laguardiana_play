package machines.status;

import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
public class MachineEnvelopeDepositStatus extends MachineStatus {

    public MachineEnvelopeDepositStatus(Integer currentUserId, String neededController, String neededAction, String stateName, String message) {
        super(currentUserId, neededAction, stateName, message);
    }

    public EnvelopeDeposit getCurrentDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
