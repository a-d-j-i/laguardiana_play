/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state.poll;

import devices.glory.state.poll.Reset;
import devices.glory.state.poll.GloryDE50StatePoll;
import static devices.device.DeviceStatus.STATUS.JAM;
import devices.glory.GloryDE50Device.GloryDE50StateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.Error;
import devices.glory.state.Error.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import play.Logger;

/**
 *
 * @author adji
 */
public class StoringErrorReset extends GloryDE50StatePoll {

    int retries = 100;

    public StoringErrorReset(GloryDE50StateApi api) {
        super(api);
        getApi().setClosing(false);
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
        Logger.debug("STORING_ERROR_RESET_COMMAND");

        switch (lastResponse.getD1Mode()) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
                switch (lastResponse.getSr1Mode()) {
                    case abnormal_device:
                        return new Reset(getApi(), this);
                    case storing_error:
                    case escrow_open_request:
                        if (getApi().isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            getApi().notifyListeners(JAM);
                            break;
                        }
                        getApi().setClosing(false);
                        return sendGloryOperation(new devices.glory.operation.OpenEscrow());
                    case escrow_open:
                        getApi().setClosing(false);
                        break;
                    case being_recover_from_storing_error:
                        if (getApi().isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            getApi().notifyListeners(JAM);
                            break;
                        }
                        return sendGloryOperation(new devices.glory.operation.ResetDevice());
                    case being_reset:
                        break;
                    case escrow_close_request:
                        if (getApi().isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            getApi().notifyListeners(JAM);
                            break;
                        }
                        GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.operation.CloseEscrow());
                        if (sret != null) {
                            return sret;
                        }
                        getApi().setClosing(true);
                        break;
                    case escrow_close:
                        getApi().setClosing(true);
                        break;
                    case storing_start_request:
                        return sendGloryOperation(new devices.glory.operation.StoringStart(0));
                    case being_store:
                        break;
                    case waiting:
                        return sendGloryOperation(new devices.glory.operation.RemoteCancel());
                    default:
                        return new Error(getApi(), COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                                String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", lastResponse.getSr1Mode().name()));
                }
                break;
            case neutral:
                switch (lastResponse.getSr1Mode()) {
                    case storing_error:
                        return sendGloryOperation(new devices.glory.operation.SetStroringErrorRecoveryMode());
                    case waiting:
                        return this;
                    default:
                        return new Error(getApi(), COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                                String.format("StoringErrorResetCommand Abnormal device Invalid SR1-1 mode %s", lastResponse.getSr1Mode().name()));
                }
            case deposit:
            case collect_mode:
            case manual:
            case initial:
                switch (lastResponse.getSr1Mode()) {
                    case storing_start_request:
                        return sendGloryOperation(new devices.glory.operation.OpenEscrow());
                    default:
                        return sendGloryOperation(new devices.glory.operation.RemoteCancel());
                }
            default:
                return new Error(getApi(), COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                        String.format("StoringErrorResetCommand Invalid D1-4 mode %s", lastResponse.getD1Mode().name()));
        }
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
