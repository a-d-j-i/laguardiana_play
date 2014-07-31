package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50ResponseWithData;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.collect_mode;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.deposit;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.initial;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.manual;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.neutral;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.normal_error_recovery_mode;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.storing_error_recovery_mode;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.abnormal_device;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_recover_from_storing_error;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_reset;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_store;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_open;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_open_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_error;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_start_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.waiting;
import devices.glory.state.GloryDE50StateError;
import devices.glory.state.GloryDE50StateError.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.JAM;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateStoringErrorReset extends GloryDE50StatePoll {

    int retries = 100;

    public GloryDE50StateStoringErrorReset(GloryDE50Device api) {
        super(api);
        api.setClosing(false);
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50ResponseWithData lastResponse) {
        Logger.debug("STORING_ERROR_RESET_COMMAND");

        switch (lastResponse.getD1Mode()) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
                switch (lastResponse.getSr1Mode()) {
                    case abnormal_device:
                        return new GloryDE50StateReset(api, this);
                    case storing_error:
                    case escrow_open_request:
                        if (api.isClosing()) {
                            /*setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        api.setClosing(false);
                        return sendGloryOperation(new devices.glory.operation.OpenEscrow());
                    case escrow_open:
                        api.setClosing(false);
                        break;
                    case being_recover_from_storing_error:
                        if (api.isClosing()) {
                            /*setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        return sendGloryOperation(new devices.glory.operation.ResetDevice());
                    case being_reset:
                        break;
                    case escrow_close_request:
                        if (api.isClosing()) {
                            /*setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return;*/
                            api.notifyListeners(JAM);
                            break;
                        }
                        GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.operation.CloseEscrow());
                        if (sret != null) {
                            return sret;
                        }
                        api.setClosing(true);
                        break;
                    case escrow_close:
                        api.setClosing(true);
                        break;
                    case storing_start_request:
                        return sendGloryOperation(new devices.glory.operation.StoringStart(0));
                    case being_store:
                        break;
                    case waiting:
                        return sendGloryOperation(new devices.glory.operation.RemoteCancel());
                    default:
                        return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
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
                        return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
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
                return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                        String.format("StoringErrorResetCommand Invalid D1-4 mode %s", lastResponse.getD1Mode().name()));
        }
        return this;
    }

}
