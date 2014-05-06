/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device;
import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import devices.glory.command.GloryOperationAbstract;
import devices.glory.response.GloryDE50Response;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import java.util.Map;
import play.Logger;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract {

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
            GloryDE50Response response = api.sendGloryOperation(cmd);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean envelopeDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean collect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storingErrorReset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean cancelDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean withdrawDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Integer getCurrency() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
