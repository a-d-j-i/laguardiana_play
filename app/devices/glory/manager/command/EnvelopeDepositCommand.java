/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.ManagerInterface;

/**
 *
 * @author adji
 */
public class EnvelopeDepositCommand extends ManagerCommandAbstract {

    private final CommandData commandData;

    public EnvelopeDepositCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
        commandData = new CommandData();
    }

    public void storeDeposit(Integer sequenceNumber) {
        commandData.storeDeposit();
    }

    @Override
    public void execute() {
        if (!gotoNeutral(false, false, true)) {
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
        setState(ManagerInterface.State.PUT_THE_ENVELOPE_IN_THE_ESCROW);
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
                    if (commandData.needToStoreDeposit()) {
                        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                            return;
                        }
                        setState(ManagerInterface.State.STORING);
                        break;
                    } else {
                        setState(ManagerInterface.State.READY_TO_STORE);
                    }
                    break;

                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        gotoNeutral(true, false, true);
                        return;
                    }
                    if (!gloryStatus.isEscrowBillPresent()) {
                        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                            return;
                        }
                    } else {
                        setState(ManagerInterface.State.READY_TO_STORE);
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;
                case abnormal_device:
                    setError(ManagerInterface.Error.JAM,
                            String.format("EnvelopeDeposit Abnormal device, todo: get the flags"));
                    return;
                case storing_error:
                    setError(ManagerInterface.Error.STORING_ERROR_CALL_ADMIN,
                            String.format("EnvelopeDeposit Storing error, todo: get the flags"));
                    return;
                default:
                    setError(ManagerInterface.Error.APP_ERROR,
                            String.format("EnvelopeDeposit invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            setState(ManagerInterface.State.CANCELING);
        }
        gotoNeutral(true, false, false);
    }
}