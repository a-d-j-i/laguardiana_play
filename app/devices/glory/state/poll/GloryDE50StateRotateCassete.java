package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50ResponseWithData;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class GloryDE50StateRotateCassete extends GloryDE50StatePoll {

    final GloryDE50StateAbstract prevStep;

    public GloryDE50StateRotateCassete(GloryDE50Device api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public GloryDE50StateAbstract init() {
        return prevStep;
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50ResponseWithData lastResponse) {
        return prevStep;
    }

}
