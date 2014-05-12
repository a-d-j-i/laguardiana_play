package devices.glory.state.poll;

import devices.glory.state.poll.GloryDE50StatePoll;
import devices.glory.GloryDE50Device.GloryDE50StateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class RotateCassete extends GloryDE50StatePoll {

    final GloryDE50StateAbstract prevStep;

    public RotateCassete(GloryDE50StateApi api, GloryDE50StateAbstract prevStep) {
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
