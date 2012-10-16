/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManager.ThreadCommandApi;

/**
 *
 * @author adji
 */
public class EnvelopeDepositCommand extends ManagerCommandAbstract {

    public EnvelopeDepositCommand(ThreadCommandApi threadCommandApi) {
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
        setState(GloryManager.State.PUT_THE_ENVELOPE_IN_THE_ESCROW);
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
                        sleep(2000);
                        if (!sendGCommand(new devices.glory.command.CloseEscrow())) {
                            // Ignore the error, could happen if some one takes the envelope quickly.
                        }
                    }
                    break;
                case storing_start_request:
                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        setState(GloryManager.State.IDLE);
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
                        setState(GloryManager.State.STORING);
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;
                case abnormal_device:
                    setError(GloryManager.Error.JAM,
                            String.format("EnvelopeDeposit Abnormal device, todo: get the flags"));
                    return;
                case storing_error:
                    setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN,
                            String.format("EnvelopeDeposit Storing error, todo: get the flags"));
                    return;
                default:
                    setError(GloryManager.Error.APP_ERROR,
                            String.format("EnvelopeDeposit invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            setState(GloryManager.State.CANCELING);
        }
        gotoNeutral(true, false);
    }
}
