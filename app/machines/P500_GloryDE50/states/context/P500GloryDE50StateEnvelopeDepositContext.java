package machines.P500_GloryDE50.states.context;

import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateEnvelopeDepositContext extends P500GloryDE50StateContext {

    private final Integer currentUserId;
    private final Integer depositId;

    public P500GloryDE50StateEnvelopeDepositContext(P500GloryDE50StateContext context, EnvelopeDeposit d) {
        super(context.machine, context.glory);
        this.depositId = d.depositId;
        this.currentUserId = d.user.userId;
    }

    public Integer getDepositId() {
        return depositId;
    }

    public EnvelopeDeposit getEnvelopeDeposit() {
        return EnvelopeDeposit.findById(depositId);
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateEnvelopeDepositContext{" + "currentUserId=" + currentUserId + ", depositId=" + depositId + '}';
    }

}
