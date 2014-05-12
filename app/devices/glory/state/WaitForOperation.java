/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateApi;

/**
 *
 * @author adji
 */
public class WaitForOperation extends GloryDE50StateAbstract {

    public WaitForOperation(GloryDE50StateApi api) {
        super(api);
    }

    @Override
    public boolean acceptCollect() {
        return true;
    }

    @Override
    public boolean acceptCount() {
        return true;
    }

    @Override
    public boolean acceptEnvelopeDeposit() {
        return true;
    }

    @Override
    public boolean acceptReset() {
        return true;
    }

    @Override
    public boolean acceptStoringReset() {
        return true;
    }

    @Override
    public boolean acceptOpenPort() {
        return true;
    }
}
