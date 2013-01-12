/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.GloryManager;
import devices.glory.manager.ManagerInterface;
import play.Logger;

/**
 *
 * @author adji
 */
public class StoringErrorResetCommand extends ManagerCommandAbstract {

    public StoringErrorResetCommand(GloryManager.ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        for (int i = 0; i < retries; i++) {
            boolean avoidCancel = false;
            Logger.debug("StoringErrorReset command");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getD1Mode()) {
                case storing_error_recovery_mode:
                    switch (gloryStatus.getSr1Mode()) {
                        case storing_error:
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                return;
                            }
                            avoidCancel = true;
                            break;
                        case escrow_open:
                            break;
                        case being_recover_from_storing_error:
                            avoidCancel = true;
                            if (!sendGloryCommand(new devices.glory.command.ResetDevice())) {
                                return;
                            }
                            break;
                        case being_reset:
                            break;
                        case escrow_close_request:
                            avoidCancel = true;
                            if (!sendGloryCommand(new devices.glory.command.CloseEscrow())) {
                                return;
                            }
                        case escrow_close:
                            break;
                        case storing_start_request:
                            avoidCancel = true;
                            if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                                return;
                            }
                        case being_store:
                            break;
                        case waiting:
                            sendRemoteCancel();
                            break;

                        default:
                            setError(ManagerInterface.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name()));
                            break;
                    }
                    break;
                case neutral:
                    switch (gloryStatus.getSr1Mode()) {
                        case storing_error:
                            if (!sendGloryCommand(new devices.glory.command.SetStroringErrorRecoveryMode())) {
                                return;
                            }
                            break;
                        case waiting:
                            clearError();
                            return;
                        default:
                            setError(ManagerInterface.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name()));
                            break;
                    }
                    break;
                case normal_error_recovery_mode:
                case deposit:
                case collect_mode:
                case manual:
                case initial:
                    if (sendRemoteCancel()) {
                        if (gloryStatus.getSr1Mode() != GloryStatus.SR1Mode.storing_error) {
                            clearError();
                            return;
                        }
                    }
                    break;
                default:
                    setError(ManagerInterface.Error.APP_ERROR,
                            String.format("gotoNeutralInvalid D1-4 mode %s", gloryStatus.getD1Mode().name()));
                    break;
            }
            if (mustCancel() && !avoidCancel) {
                break;
            }
            sleep();
        }
        if (!mustCancel()) {
            setError(ManagerInterface.Error.APP_ERROR, "GOTO NEUTRAL TIMEOUT");
            Logger.debug("GOTO NEUTRAL TIMEOUT!!!");
        }

        Logger.debug("GOTO NEUTRAL DONE CANCEL");
        return;
    }
}
