package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import devices.glory.response.GloryDE50OperationResponse;

/**
 *
 * @author adji
 */
public class RotateCassete extends GloryDE50StatePoll {

    final GloryDE50StateAbstract prevStep;

    public RotateCassete(GloryDE50StateMachineApi api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public GloryDE50StateAbstract init() {
        return prevStep;
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
        return prevStep;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return null;
    }

}
