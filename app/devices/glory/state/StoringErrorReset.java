/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import static devices.glory.GloryDE50Device.STATUS.JAM;
import devices.glory.response.GloryDE50Response;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import play.Logger;

/**
 *
 * @author adji
 */
public class StoringErrorReset extends GloryDE50StatePoll {

    int retries = 100;

    public StoringErrorReset(GloryDE50StateMachineApi api) {
        super(api);
        api.setClosing(false);
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50Response lastResponse) {
        Logger.debug("STORING_ERROR_RESET_COMMAND");

        switch (lastResponse.getD1Mode()) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
                switch (lastResponse.getSr1Mode()) {
                    case abnormal_device:
                        return new Reset(api, this);
                    case storing_error:
                    case escrow_open_request:
                        if (api.isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        api.setClosing(false);
                        return sendGloryOperation(new devices.glory.command.OpenEscrow());
                    case escrow_open:
                        api.setClosing(false);
                        break;
                    case being_recover_from_storing_error:
                        if (api.isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        return sendGloryOperation(new devices.glory.command.ResetDevice());
                    case being_reset:
                        break;
                    case escrow_close_request:
                        if (api.isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.command.CloseEscrow());
                        if (sret != null) {
                            return sret;
                        }
                        api.setClosing(true);
                        break;
                    case escrow_close:
                        api.setClosing(true);
                        break;
                    case storing_start_request:
                        return sendGloryOperation(new devices.glory.command.StoringStart(0));
                    case being_store:
                        break;
                    case waiting:
                        return sendGloryOperation(new devices.glory.command.RemoteCancel());
                    default:
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                                String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", lastResponse.getSr1Mode().name()));
                }
                break;
            case neutral:
                switch (lastResponse.getSr1Mode()) {
                    case storing_error:
                        return sendGloryOperation(new devices.glory.command.SetStroringErrorRecoveryMode());
                    case waiting:
                        return this;
                    default:
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                                String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", lastResponse.getSr1Mode().name()));
                }
            case deposit:
            case collect_mode:
            case manual:
            case initial:
                switch (lastResponse.getSr1Mode()) {
                    case storing_start_request:
                        return sendGloryOperation(new devices.glory.command.OpenEscrow());
                    default:
                        return sendGloryOperation(new devices.glory.command.RemoteCancel());
                }
            default:
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                        String.format("StoringErrorResetCommand Invalid D1-4 mode %s", lastResponse.getD1Mode().name()));
        }
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
