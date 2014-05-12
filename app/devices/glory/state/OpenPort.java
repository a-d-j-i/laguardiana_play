/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.state.poll.GotoNeutral;
import devices.glory.GloryDE50Device.GloryDE50StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class OpenPort extends GloryDE50StateAbstract {

    final String port;

    public OpenPort(GloryDE50StateApi api, String port) {
        super(api);
        this.port = port;
    }

    @Override
    public GloryDE50StateAbstract step() {
        // wait for operations
        super.step();
        if (getApi().open(port)) {
            Logger.debug("Port open success");
            return new GotoNeutral(getApi());
        }
        Logger.debug("Port not open, polling");
        return this;
    }
}
