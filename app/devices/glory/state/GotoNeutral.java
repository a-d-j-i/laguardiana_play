/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device;
import static devices.glory.GloryDE50Device.STATUS.*;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import devices.glory.response.GloryDE50Response;
import static devices.glory.response.GloryDE50Response.D1Mode.collect_mode;
import static devices.glory.response.GloryDE50Response.D1Mode.deposit;
import static devices.glory.response.GloryDE50Response.D1Mode.initial;
import static devices.glory.response.GloryDE50Response.D1Mode.manual;
import static devices.glory.response.GloryDE50Response.D1Mode.neutral;
import static devices.glory.response.GloryDE50Response.D1Mode.normal_error_recovery_mode;
import static devices.glory.response.GloryDE50Response.D1Mode.storing_error_recovery_mode;
import static devices.glory.response.GloryDE50Response.SR1Mode.abnormal_device;
import static devices.glory.response.GloryDE50Response.SR1Mode.being_exchange_the_cassette;
import static devices.glory.response.GloryDE50Response.SR1Mode.being_recover_from_storing_error;
import static devices.glory.response.GloryDE50Response.SR1Mode.being_reset;
import static devices.glory.response.GloryDE50Response.SR1Mode.being_restoration;
import static devices.glory.response.GloryDE50Response.SR1Mode.counting;
import static devices.glory.response.GloryDE50Response.SR1Mode.counting_start_request;
import static devices.glory.response.GloryDE50Response.SR1Mode.escrow_close;
import static devices.glory.response.GloryDE50Response.SR1Mode.escrow_close_request;
import static devices.glory.response.GloryDE50Response.SR1Mode.escrow_open;
import static devices.glory.response.GloryDE50Response.SR1Mode.escrow_open_request;
import static devices.glory.response.GloryDE50Response.SR1Mode.storing_error;
import static devices.glory.response.GloryDE50Response.SR1Mode.storing_start_request;
import static devices.glory.response.GloryDE50Response.SR1Mode.waiting;
import static devices.glory.response.GloryDE50Response.SR1Mode.waiting_for_an_envelope_to_set;
import java.util.Date;
import play.Logger;

/**
 *
 * @author adji
 */
public class GotoNeutral extends GloryDE50StatePoll {

    boolean bagRotated = false;
    int remoteCancelRetries = 5;
    boolean canOpenEscrow = false;
    boolean forceEmptyHoper = false;
    final GloryDE50StateAbstract prevState;

    public GotoNeutral(GloryDE50Device.GloryDE50StateMachineApi api) {
        super(api);
        this.prevState = new ReadyForCommand(api);
    }

    public GotoNeutral(GloryDE50Device.GloryDE50StateMachineApi api, GloryDE50StateAbstract prevState, boolean canOpenEscrow, boolean forceEmptyHoper) {
        super(api);
        this.prevState = prevState;
        this.canOpenEscrow = canOpenEscrow;
        this.forceEmptyHoper = forceEmptyHoper;
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50Response lastResponse) {
        GloryDE50StateAbstract sret;
        Logger.debug("GOTO NEUTRAL %s %s",
                (canOpenEscrow ? "OPEN ESCROW" : ""),
                (forceEmptyHoper ? "FORCE EMPTY HOPER" : ""));

        switch (lastResponse.getSr1Mode()) {
            case storing_error:
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error must call admin");
        }
        if (lastResponse.isCassetteFullCounter()) {
            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.CASSETE_FULL, "Cassete Full");
        }
        switch (lastResponse.getD1Mode()) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
            case deposit:
            case collect_mode:
            case manual:
                switch (lastResponse.getSr1Mode()) {
                    case escrow_open_request:
                        if (api.isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return false;*/
                            api.setClosing(false);
                            api.notifyListeners(JAM);
                            break;
                        }
                        /*                            if (!canOpenEscrow) {
                         setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                         "There are bills in the escrow call an admin 1"));
                         return false;
                         }*/
                        api.setClosing(false);
                        sendGloryOperation(new devices.glory.command.OpenEscrow());
                        break;
                    case abnormal_device:
                        api.setClosing(false);
                        api.notifyListeners(JAM);
                        if (lastResponse.getD1Mode() == GloryDE50Response.D1Mode.normal_error_recovery_mode) {
                            return new Reset(api, this);
                        } else {
                            sret = sendGloryOperation(new devices.glory.command.RemoteCancel());
                            if (sret != null) {
                                return sret;
                            }
                        }
                        break;
                    case escrow_close_request:
                    case being_recover_from_storing_error:
                        if (api.isClosing()) {
                            /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                             "Escrow door jamed"));
                             return false;*/
                            api.setClosing(false);
                            api.notifyListeners(JAM);
                            break;
                        }
                        if (lastResponse.isEscrowBillPresent()) {
                            break;
                        }
                    // don't break
                    case waiting_for_an_envelope_to_set:
                        sret = sendGloryOperation(new devices.glory.command.CloseEscrow());
                        if (sret != null) {
                            // ignore ? 
                            //return this;
                            return sret;
                        }
                        api.setClosing(true);
                        break;
                    case being_reset:
                        break;
                    case escrow_close: // The escrow is closing... wait.
                        api.setClosing(true);
                        break;
                    case counting: // Japaneese hack...
                        break;
                    case being_restoration:
                        api.notifyListeners(REMOVE_THE_BILLS_FROM_ESCROW);
                        break;
                    case escrow_open:
                        api.notifyListeners(REMOVE_THE_BILLS_FROM_ESCROW);
                        api.setClosing(false);
                        break;
                    case storing_error:
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error, todo: get the flags");
                    case storing_start_request:
                        if (!canOpenEscrow) {
                            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.BILLS_IN_ESCROW_CALL_ADMIN, "There are bills in the escrow call an admin 2");
                        }
                        api.setClosing(false);
                        sendGloryOperation(new devices.glory.command.OpenEscrow());
                        break;
                    case counting_start_request:
                    case being_exchange_the_cassette:
                    case waiting:
                        if (lastResponse.isRejectBillPresent()) {
                            api.notifyListeners(REMOVE_REJECTED_BILLS);
                            break;
                        }
                        if (lastResponse.isHopperBillPresent()) {
                            api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                            break;
                        }
                        sret = sendGloryOperation(new devices.glory.command.RemoteCancel());
                        if (sret != null) {
                            remoteCancelRetries--;
                            if (remoteCancelRetries <= 0) {
                                return sret;
                            }
                        }
                        break;
                    default:
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                                String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", lastResponse.getSr1Mode().name()));
                }
                break;
            case initial:
                sret = sendGloryOperation(new devices.glory.command.RemoteCancel());
                if (sret != null) {
                    return sret;
                }
                break;
            case neutral:
                switch (lastResponse.getSr1Mode()) {
                    case abnormal_device:
                        notifyListeners(JAM);
                        sret = sendGloryOperation(new devices.glory.command.SetErrorRecoveryMode());
                        if (sret != null) {
                            return sret;
                        }
                        break;
                    case waiting:
                        if (forceEmptyHoper) {
                            if (lastResponse.isRejectBillPresent()) {
                                api.notifyListeners(REMOVE_REJECTED_BILLS);
                                break;
                            } else if (lastResponse.isHopperBillPresent()) {
                                api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                                break;
                            }
                        }
                        if (lastResponse.isEscrowBillPresent()) {
                            if (!canOpenEscrow) {
                                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.BILLS_IN_ESCROW_CALL_ADMIN, "There are bills in the escrow call an admin 3");
                            }
                            api.setClosing(false);
                            sret = sendGloryOperation(new devices.glory.command.OpenEscrow());
                            if (sret != null) {
                                break;
                            }
                        }
                        if (!bagRotated) {
                            // Rotate the bag once to fix the glory proble.
                            bagRotated = true;
                            // set the time if possible, some times it fails, ignroe this
                            sret = sendGloryOperation(new devices.glory.command.SetTime(new Date()));
                            if (sret != null) {
                                String error = lastResponse.getError();
                                Logger.error("Error %s sending cmd SetTime", error);
                                break;
                            }
                            // Rotate if possible, some time it fails, ignore this
                            sret = sendGloryOperation(new devices.glory.command.SetCollectMode());
                            if (sret != null) {
                                String error = lastResponse.getError();
                                Logger.error("Error %s sending cmd SetCollectMode", error);
                                break;
                            }
                            break;
                        }
                        notifyListeners(NEUTRAL);
                        Logger.debug("GOTO NEUTRAL DONE");
                        return prevState;
                    default:
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                                String.format("gotoNeutral Abnormal device Invalid SR1-2 mode %s", lastResponse.getSr1Mode().name()));
                }
                break;
            default:
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR,
                        String.format("gotoNeutralInvalid D1-4 mode %s", lastResponse.getD1Mode().name()));
        }
        Logger.debug("GOTO NEUTRAL DONE");
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
