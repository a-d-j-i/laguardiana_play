/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50ResponseWithData;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_reset;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_restoration;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class GloryDE50StateReset extends GloryDE50StatePoll {

    final GloryDE50StateAbstract prevStep;

    public GloryDE50StateReset(GloryDE50Device api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public GloryDE50StateAbstract init() {
        return sendGloryOperation(new devices.glory.operation.ResetDevice());
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50ResponseWithData lastResponse) {
        switch (lastResponse.getSr1Mode()) {
            case being_reset:
            case being_restoration:
                return this;
            default:
                return prevStep;
        }
    }

    @Override
    public String toString() {
        return "GloryDE50StateReset{" + "prevStep=" + prevStep + '}';
    }

}