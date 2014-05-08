package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import static devices.glory.GloryDE50Device.STATUS.JAM;
import static devices.glory.GloryDE50Device.STATUS.PUT_THE_ENVELOPE_IN_THE_ESCROW;
import static devices.glory.GloryDE50Device.STATUS.READY_TO_STORE;
import devices.glory.response.GloryDE50OperationResponse;
import static devices.glory.response.GloryDE50OperationResponse.SR1Mode.waiting_for_an_envelope_to_set;
import devices.glory.state.Error.COUNTER_CLASS_ERROR_CODE;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDeposit extends GloryDE50StatePoll {

    public EnvelopeDeposit(GloryDE50StateMachineApi api) {
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
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
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
                notifyListeners(PUT_THE_ENVELOPE_IN_THE_ESCROW);
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
                return new WaitForStoreCommand(api, this);

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
                    notifyListeners(READY_TO_STORE);
                }
                break;
            case being_store:
                storeTry = true;
                break;
            case abnormal_device:
                if (api.isClosing()) {
                    /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                     "Escrow door jamed"));*/
                    //return;
                }
                notifyListeners(JAM);
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
                return new Error(api, COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                        String.format("EnvelopeDeposit Storing error, todo: get the flags"));
            default:
                return new Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_MANAGER_ERROR,
                        String.format("EnvelopeDeposit invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
        }
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }
}
