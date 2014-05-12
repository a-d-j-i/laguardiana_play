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
public class ReadyToStore extends GloryDE50StateAbstract {

    final GloryDE50StateAbstract prevStep;
    boolean sended = false;

    public ReadyToStore(GloryDE50StateApi api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public boolean acceptStore() {
        return true;
    }

    @Override
    public boolean acceptWithdraw() {
        return true;
    }
}
