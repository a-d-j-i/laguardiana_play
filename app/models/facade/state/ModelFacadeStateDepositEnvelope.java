package models.facade.state;

import models.facade.state.substate.ModelFacadeSubStateApi;
import models.ModelFacade;
import controllers.EnvelopeDepositController;
import controllers.Secure;
import java.util.Date;
import models.EnvelopeDeposit;
import models.facade.state.substate.envelope_deposit.EnvelopeDepositStart;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgEnvelopeContent.EnvelopeContentType;
import models.facade.status.ModelFacadeStateStatus;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class ModelFacadeStateDepositEnvelope extends ModelFacadeStateAbstract {

    private final EnvelopeDepositController.EvenlopeDepositData data;
    protected Integer currentDepositId = null;

    public ModelFacadeStateDepositEnvelope(ModelFacade.ModelFacadeStateApi api, EnvelopeDepositController.EvenlopeDepositData data) {
        super(api);
        this.data = data;
    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("EnvelopeDepositController", subState, data, currentDepositId);
    }

    @Override
    public ModelFacadeStateAbstract init() {
        LgEnvelope envelope = new LgEnvelope(0, data.envelopeCode);
        if (data.cashData.amount > 0) {
            envelope.addContent(new LgEnvelopeContent(EnvelopeContentType.CASH, data.cashData.amount, data.cashData.currency.numericId));
        }
        if (data.checkData.amount > 0) {
            envelope.addContent(new LgEnvelopeContent(EnvelopeContentType.CHECKS, data.checkData.amount, data.checkData.currency.numericId));
        }
        if (data.ticketData.amount > 0) {
            envelope.addContent(new LgEnvelopeContent(EnvelopeContentType.TICKETS, data.ticketData.amount, data.ticketData.currency.numericId));
        }
        if (data.hasDocuments != null && data.hasDocuments) {
            envelope.addContent(new LgEnvelopeContent(EnvelopeContentType.DOCUMENTS, null, null));
        }
        if (data.hasOthers != null && data.hasOthers) {
            envelope.addContent(new LgEnvelopeContent(EnvelopeContentType.OTHERS, null, null));
        }
        envelope.save();
        EnvelopeDeposit deposit = new EnvelopeDeposit(Secure.getCurrentUser(), data.reference2, (DepositUserCodeReference) data.reference1.lov);
        deposit.startDate = new Date();
        deposit.addEnvelope(envelope);
        deposit.save();
        currentDepositId = deposit.depositId;
        api.getMachine().startEnvelopeDeposit();
        deposit.printStart();
        subState = new EnvelopeDepositStart(new ModelFacadeSubStateApi(api));
        return this;
    }

    @Override
    public boolean finish() {
        if (currentDepositId != null) {
            EnvelopeDeposit d = EnvelopeDeposit.findById(currentDepositId);
            if (d != null) {
                if (d.closeDate != null && d.finishCause == LgDeposit.FinishCause.FINISH_CAUSE_OK) {
                    d.print(false);
                }
                d.finishDate = new Date();
                d.save();
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ModelFacadeStateEnvelopeDeposit";
    }
}
