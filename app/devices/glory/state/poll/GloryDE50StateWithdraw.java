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
public class GloryDE50StateWithdraw extends GloryDE50StatePoll {

    public GloryDE50StateWithdraw(GloryDE50Device api) {
        super(api);
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
                return this;
        }
    }

}
