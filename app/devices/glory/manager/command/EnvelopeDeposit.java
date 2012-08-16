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
        boolean storeTry = false;
        while (!mustCancel()) {
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case escrow_open:
                case waiting_for_an_envelope_to_set:
                    break;
                case escrow_close_request:
                    WaitForEmptyEscrow();
                    break;
                case storing_start_request:
                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        threadCommandApi.setStatus(Manager.Status.IDLE);
                        gotoNeutral(true, false);
                        return;
                    }
                    if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                        return;
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;
                case abnormal_device:
                    threadCommandApi.setError(String.format("EnvelopeDeposit Abnormal device, todo: get the flags"));
                    return;
                case storing_error:
                    threadCommandApi.setError(String.format("EnvelopeDeposit Storing error, todo: get the flags"));
                    return;
                default:
                    threadCommandApi.setError(String.format("EnvelopeDeposit invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        gotoNeutral(true, false);
    }
}
