/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import devices.glory.response.GloryDE50Response;

/**
 *
 * @author adji
 */
public class Reset extends GloryDE50StatePoll {

    final GloryDE50StateAbstract prevStep;

    public Reset(GloryDE50StateMachineApi api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public GloryDE50StateAbstract init() {
        return sendGloryOperation(new devices.glory.command.ResetDevice());
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50Response lastResponse) {
        switch (lastResponse.getSr1Mode()) {
            case being_reset:
            case being_restoration:
                return this;
            default:
                return prevStep;
        }
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
