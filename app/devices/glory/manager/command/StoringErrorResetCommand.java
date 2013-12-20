/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManagerError;
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
    public void run() {
        // retry closing at least once.
        threadCommandApi.setClosing(false);
        for (int i = 0; i < retries && !mustCancel(); i++) {
            Logger.debug("StoringErrorReset command");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getD1Mode()) {
                case normal_error_recovery_mode:
                case storing_error_recovery_mode:
                    switch (gloryStatus.getSr1Mode()) {
                        case abnormal_device:
                            resetDevice();
                            break;
                        case storing_error:
                        case escrow_open_request:
                            if (threadCommandApi.isClosing()) {
                                /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                                 "Escrow door jamed"));
                                 return;*/
                                setState(ManagerInterface.MANAGER_STATE.JAM);
                                break;
                            }
                            if (!openEscrow()) {
                                return;
                            }
                            break;
                        case escrow_open:
                            threadCommandApi.setClosing(false);
                            break;
                        case being_recover_from_storing_error:
                            if (threadCommandApi.isClosing()) {
                                /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                                 "Escrow door jamed"));
                                 return;*/
                                setState(ManagerInterface.MANAGER_STATE.JAM);
                                break;
                            }
                            if (!sendGloryCommand(new devices.glory.command.ResetDevice())) {
                                return;
                            }
                            break;
                        case being_reset:
                            break;
                        case escrow_close_request:
                            if (threadCommandApi.isClosing()) {
                                /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                                 "Escrow door jamed"));
                                 return;*/
                                setState(ManagerInterface.MANAGER_STATE.JAM);
                                break;
                            }
                            if (!closeEscrow()) {
                                return;
                            }
                            break;
                        case escrow_close:
                            threadCommandApi.setClosing(true);
                            break;
                        case storing_start_request:
                            if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                                return;
                            }
                        case being_store:
                            break;
                        case waiting:
                            if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                return;
                            }
                            break;
                        default:
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                    String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name())));
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
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                    String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name())));
                            break;
                    }
                    break;
                case deposit:
                case collect_mode:
                case manual:
                case initial:
                    switch (gloryStatus.getSr1Mode()) {
                        case storing_start_request:
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                return;
                            }
                            break;
                        default:
                            if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                return;
                            }
                            break;
                    }
                default:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                            String.format("StoringErrorResetCommand Invalid D1-4 mode %s", gloryStatus.getD1Mode().name())));
                    break;
            }
            sleep();
        }
        if (!mustCancel()) {
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "GOTO NEUTRAL TIMEOUT"));
            Logger.debug("StoringErrorReset TIMEOUT!!!");
        }

        Logger.debug("StoringErrorReset DONE CANCEL");
    }
}
