package devices.glory.manager.command;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.GloryManagerError;
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
    public void run() {
        if (!gotoNeutral(false, true)) {
            return;
        }
        if (!sendGCommand(new devices.glory.command.SetManualMode())) {
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                    String.format("EnvelopeDepositCommand gotoManualDeposit Error %s", gloryStatus.getLastError())));
            return;
        }
        boolean storeTry = false;
        while (!mustCancel()) {
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case escrow_open:
                    setState(ManagerInterface.MANAGER_STATE.PUT_THE_ENVELOPE_IN_THE_ESCROW);
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
                        setState(ManagerInterface.MANAGER_STATE.STORING);
                        break;
                    } else {
                        setState(ManagerInterface.MANAGER_STATE.READY_TO_STORE);
                    }
                    break;

                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        gotoNeutral(true, true);
                        return;
                    }
                    if (!gloryStatus.isEscrowBillPresent()) {
                        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                            return;
                        }
                    } else {
                        setState(ManagerInterface.MANAGER_STATE.READY_TO_STORE);
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;
                case abnormal_device:
                    setState(ManagerInterface.MANAGER_STATE.JAM);
                    break;
                case storing_error:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                            String.format("EnvelopeDeposit Storing error, todo: get the flags")));
                    return;
                default:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                            String.format("EnvelopeDeposit invalid sr1 mode %s", gloryStatus.getSr1Mode().name())));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            setState(ManagerInterface.MANAGER_STATE.CANCELING);
        }
        gotoNeutral(true, false);
    }
}
