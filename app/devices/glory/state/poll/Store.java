/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state.poll;

import devices.glory.GloryDE50Device.GloryDE50StateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class Store extends GloryDE50StatePoll {

    public Store(GloryDE50StateApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract init() {
        return sendGloryOperation(new devices.glory.operation.ResetDevice());
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
        switch (lastResponse.getSr1Mode()) {
            case being_reset:
            case being_restoration:
                return this;
            default:
                return this;
        }
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
