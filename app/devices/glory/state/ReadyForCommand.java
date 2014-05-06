/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device;
import play.Logger;

/**
 *
 * @author adji
 */
public class ReadyForCommand extends GloryDE50StateAbstract {

    public ReadyForCommand(GloryDE50Device.GloryDE50StateMachineApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract step() {
        Logger.debug("ReadyForCommand");
        return this;
    }
}
