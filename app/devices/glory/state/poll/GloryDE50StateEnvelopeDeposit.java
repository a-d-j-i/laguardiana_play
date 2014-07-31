package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50ResponseWithData;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.abnormal_device;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_store;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_open;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_error;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_start_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.waiting;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.waiting_for_an_envelope_to_set;
import devices.glory.state.GloryDE50StateError;
import devices.glory.state.GloryDE50StateError.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.GloryDE50StateReadyToStore;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.JAM;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.PUT_THE_ENVELOPE_IN_THE_ESCROW;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.READY_TO_STORE;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateEnvelopeDeposit extends GloryDE50StatePoll {

    public GloryDE50StateEnvelopeDeposit(GloryDE50Device api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract init() {
        GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.operation.SetManualMode());
        if (sret != null) {
            return sret;
        }
        return this;
    }

    boolean storeTry = false;
    int waitForEscrow = 0;

    @Override
    public GloryDE50StateAbstract poll(GloryDE50ResponseWithData lastResponse) {
        GloryDE50StateAbstract sret;
        if (waitForEscrow == 0) {
            sret = sendGloryOperation(new devices.glory.operation.CloseEscrow());
            if (sret != null) {
                return sret;
            }
            api.setClosing(true);
        }
        if (waitForEscrow-- > 0) {
            return this;
        }
        Logger.debug("ENVELOPE_DEPOSIT_COMMAND");
        switch (lastResponse.getSr1Mode()) {
            case escrow_open:
                api.notifyListeners(PUT_THE_ENVELOPE_IN_THE_ESCROW);
                api.setClosing(false);
                break;
            case waiting_for_an_envelope_to_set:
                break;
            case escrow_close:
                api.setClosing(true);
                break;
            case escrow_close_request:
                if (lastResponse.isEscrowBillPresent()) {
                    waitForEscrow = 2000;
                }
                break;
            case storing_start_request:
                api.notifyListeners(READY_TO_STORE);
                return new GloryDE50StateReadyToStore(api, this);

            case waiting:
                // The second time after storing.
                if (storeTry) {
                    //return new GotoNeutral(api, this);
                }
                if (!lastResponse.isEscrowBillPresent()) {
                    api.setClosing(false);
                    sret = sendGloryOperation(new devices.glory.operation.OpenEscrow());
                    if (sret != null) {
                        // sret ?
                        return this;
                    }
                } else {
                    api.notifyListeners(READY_TO_STORE);
                }
                break;
            case being_store:
                storeTry = true;
                break;
            case abnormal_device:
                if (api.isClosing()) {
                    /*setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                     "Escrow door jamed"));*/
                    //return;
                }
                api.notifyListeners(JAM);
                if (lastResponse.isEscrowBillPresent()) {
                    break;
                }
                /*if (!gotoNeutral(true, true)) {
                 return;
                 }*/
                sret = sendGloryOperation(new devices.glory.operation.SetManualMode());
                if (sret != null) {
                    return sret;
                }
                break;
            case storing_error:
                return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                        String.format("EnvelopeDeposit Storing error, todo: get the flags"));
            default:
                return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                        String.format("EnvelopeDeposit invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
        }
        return this;
    }
}
