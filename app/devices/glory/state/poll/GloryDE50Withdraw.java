package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationResponse;
import static devices.glory.operation.GloryDE50OperationResponse.SR1Mode.being_reset;
import static devices.glory.operation.GloryDE50OperationResponse.SR1Mode.being_restoration;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class GloryDE50Withdraw extends GloryDE50StatePoll {

    public GloryDE50Withdraw(GloryDE50Device api) {
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
