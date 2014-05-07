/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.DeviceClassCounterIntreface;
import devices.glory.GloryDE50Device;
import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import devices.glory.operation.GloryOperationAbstract;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import play.Logger;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract implements DeviceClassCounterIntreface {

    final GloryDE50StateMachineApi api;

    public GloryDE50StateAbstract(GloryDE50StateMachineApi api) {
        this.api = api;
    }

    public GloryDE50StateAbstract init() {
        return this;
    }

    abstract public GloryDE50StateAbstract step();

    GloryDE50StateAbstract sendGloryOperation(GloryOperationAbstract cmd) {
        if (cmd != null) {
            GloryDE50OperationResponse response = api.sendGloryDE50Operation(cmd);
            if (response.isError()) {
                String error = response.getError();
                Logger.error("Error %s sending cmd : %s", error, cmd.getDescription());
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
            }
        }
        return null;
    }

    protected void notifyListeners(GloryDE50Device.STATUS status) {
        api.notifyListeners(status);
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        return false;
    }

    public boolean envelopeDeposit() {
        return false;
    }

    public boolean collect() {
        return false;
    }

    public boolean reset() {
        return false;
    }

    public boolean storingErrorReset() {
        return false;
    }

    public boolean cancelDeposit() {
        return false;
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        return false;
    }

    public boolean withdrawDeposit() {
        return false;
    }

    public Integer getCurrency() {
        return null;
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return null;
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return null;
    }

    public boolean openPort(final String pvalue, boolean wait) {
        return false;
    }

    public boolean clearError() {
        return false;
    }

    public String getError() {
        return null;
    }


    public boolean sendOperation(FutureTask<GloryDE50OperationResponse> t) {
        return false;
    }
}
