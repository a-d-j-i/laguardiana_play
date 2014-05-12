package devices.glory.state.poll;

import static devices.device.DeviceStatus.STATUS.JAM;
import static devices.device.DeviceStatus.STATUS.PUT_THE_ENVELOPE_IN_THE_ESCROW;
import static devices.device.DeviceStatus.STATUS.READY_TO_STORE;
import devices.glory.GloryDE50Device.GloryDE50StateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.Error;
import static devices.glory.response.GloryDE50OperationResponse.SR1Mode.waiting_for_an_envelope_to_set;
import devices.glory.state.Error.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.ReadyToStore;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDeposit extends GloryDE50StatePoll {

    public EnvelopeDeposit(GloryDE50StateApi api) {
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
            getApi().setClosing(true);
        }
        if (waitForEscrow-- > 0) {
            return this;
        }
        Logger.debug("ENVELOPE_DEPOSIT_COMMAND");
        switch (lastResponse.getSr1Mode()) {
            case escrow_open:
                notifyListeners(PUT_THE_ENVELOPE_IN_THE_ESCROW);
                getApi().setClosing(false);
                break;
            case waiting_for_an_envelope_to_set:
                break;
            case escrow_close:
                getApi().setClosing(true);
                break;
            case escrow_close_request:
                if (lastResponse.isEscrowBillPresent()) {
                    waitForEscrow = 2000;
                }
                break;
            case storing_start_request:
                getApi().notifyListeners(READY_TO_STORE);
                return new ReadyToStore(getApi(), this);

            case waiting:
                // The second time after storing.
                if (storeTry) {
                    //return new GotoNeutral(getApi(), this);
                }
                if (!lastResponse.isEscrowBillPresent()) {
                    getApi().setClosing(false);
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
                if (getApi().isClosing()) {
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
                return new Error(getApi(), COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                        String.format("EnvelopeDeposit Storing error, todo: get the flags"));
            default:
                return new Error(getApi(), COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                        String.format("EnvelopeDeposit invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
        }
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }
}
