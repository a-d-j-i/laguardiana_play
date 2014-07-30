/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationResponse;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.collect_mode;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.deposit;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.initial;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.manual;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.neutral;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.normal_error_recovery_mode;
import static devices.glory.operation.GloryDE50OperationResponse.D1Mode.storing_error_recovery_mode;
import static devices.glory.operation.GloryDE50OperationResponse.SR1Mode.storing_error;
import devices.glory.state.GloryDE50Error;
import devices.glory.state.GloryDE50Error.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.BAG_COLLECTED;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_THE_BILLS_FROM_HOPER;
import java.util.Date;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Collect extends GloryDE50StatePoll {

    public GloryDE50Collect(GloryDE50Device api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract init() {
        return this;
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
        GloryDE50StateAbstract sret;
        Logger.debug("COLLECT_COMMAND");

        if (!lastResponse.isCassetteFullCounter()) {
            sret = sendGloryOperation(new devices.glory.operation.SetTime(new Date()));
            if (sret != null) {
                return sret;
            }
            api.notifyListeners(BAG_COLLECTED);
            Logger.debug("COLLECT DONE");
            return new GloryDE50GotoNeutral(api);
        }
        switch (lastResponse.getSr1Mode()) {
            case storing_error:
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error must call admin");
        }
        switch (lastResponse.getD1Mode()) {
            case collect_mode:
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
            case deposit:
            case manual:
            case initial:
                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                if (lastResponse.isHopperBillPresent()) {
                    api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                    break;
                }
                sret = sendGloryOperation(new devices.glory.operation.RemoteCancel());
                if (sret != null) {
                    return sret;
                }
                break;
            case neutral:
                if (lastResponse.isCassetteFullCounter()) {
                    return new GloryDE50RotateCassete(api, this);
                }
                break;
            default:
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR,
                        String.format("gotoNeutralInvalid D1-4 mode %s", lastResponse.getD1Mode().name()));
        }

        /*        if (!mustCancel()) {
         setGloryDE50Error(new GloryDE50DeviceErrorEvent(GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, "COLLECT TIMEOUT"));
         Logger.debug("COLLECT TIMEOUT!!!");
         }
         */
        Logger.debug("COLLECT DONE CANCEL");
        return this;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
