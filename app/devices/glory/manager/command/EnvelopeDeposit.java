/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.ThreadCommandApi;

/**
 *
 * @author adji
 */
public class EnvelopeDeposit extends ManagerCommandAbstract {

    public EnvelopeDeposit(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        if (!gotoNeutral(false, false)) {
            return;
        }
        if (!sendGloryCommand(new devices.glory.command.SetManualMode())) {
            return;
        }
        if (!waitUntilD1State(GloryStatus.D1Mode.manual)) {
            return;
        }
        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
            return;
        }
        if (!waitUntilSR1State(GloryStatus.SR1Mode.escrow_open)) {
            return;
        }
        threadCommandApi.setStatus(Manager.Status.PUT_THE_ENVELOPER_IN_THE_ESCROW);
        boolean storeTry = false;
        while (!mustCancel()) {
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case escrow_open:
                    break;
                case waiting_for_an_envelope_to_set:
                    break;
                case escrow_close:
                    break;
                case escrow_close_request:
                    if (gloryStatus.isEscrowBillPresent()) {
                        if (!sendGCommand(new devices.glory.command.CloseEscrow())) {
                            // Ignore the error, could happen if some one takes the envelope quickly.
                        }
                    }
                    break;
                case storing_start_request:
                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        threadCommandApi.setStatus(Manager.Status.IDLE);
                        gotoNeutral(true, false);
                        return;
                    }
                    if (!gloryStatus.isEscrowBillPresent()) {
                        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                            return;
                        }
                    } else {
                        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                            return;
                        }
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;
                case abnormal_device:
                    threadCommandApi.setError(Manager.Error.JAM,
                            String.format("EnvelopeDeposit Abnormal device, todo: get the flags"));
                    return;
                case storing_error:
                    threadCommandApi.setError(Manager.Error.STORING_ERROR_CALL_ADMIN,
                            String.format("EnvelopeDeposit Storing error, todo: get the flags"));
                    return;
                default:
                    threadCommandApi.setError(Manager.Error.APP_ERROR,
                            String.format("EnvelopeDeposit invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        gotoNeutral(true, false);
        threadCommandApi.setStatus(Manager.Status.IDLE);
    }
}
