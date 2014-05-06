/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class OpenPort extends GloryDE50StateAbstract {

    public OpenPort(GloryDE50StateMachineApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract step() {
        Logger.debug("Glory Open Port");
        if (!api.open()) {
            Logger.error("Error openning glory port");
            api.notifyListeners("Error openning glory port");
            return this;
        } else {
            return new GotoNeutral(api);
        }
    }
}
