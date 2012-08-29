package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.Deposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgLov;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import models.lov.EnvelopeType;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class EnvelopeDepositController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static void inputReference(String reference1, String reference2) throws Throwable {

        if (reference1 != null && reference2 != null) {
            LgUser user = Secure.getCurrentUser();
            Integer ref1 = Integer.parseInt(reference1);
            LgLov userCode = DepositUserCodeReference.findByNumericId(ref1);
            if (userCode == null) {
                Logger.error("countMoney: no reference received! for %s", reference1);
            } else {
                // TODO: Finish.
                Currency currency = null;
                Deposit deposit = new Deposit(user, reference2, userCode, currency);
                deposit.save();
                getEnvelopeContents(Integer.toString(deposit.depositId), null);
                return;
            }
        }
        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        render(referenceCodes);
    }

    public static void getEnvelopeContents(String depositId, LgEnvelope envelope) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);

        if (envelope != null && envelope.envelopeTypeLov != null) {
            // TODO: Use validate.
            envelope.deposit = deposit;
            Logger.debug("envelope.envelopeTypeLov : %d", envelope.envelopeTypeLov);
            Logger.debug("envelope.number: %s", envelope.envelopeNumber);
            for (LgEnvelopeContent l : envelope.envelopeContents) {
                if (l != null) {
                    Logger.debug("content amount: %d", l.amount);
                    Logger.debug("content content: %d", l.contentTypeLov);
                    Logger.debug("content unit: %d", l.unitLov);
                }
            }
            renderArgs.put("confirm", true);
        }
        List<EnvelopeType> envelopeTypes = EnvelopeType.findAll();
        renderArgs.put("envelopeTypes", envelopeTypes);
        render(deposit, envelope);
    }

    public static void acceptEnvelope(String depositId, LgEnvelope envelope) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);

        // Validate
        if (envelope == null || envelope.envelopeTypeLov == null) {
            Logger.error("INVALID ");
            index();
            return;
        }

        // TODO: Use validate.
        envelope.deposit = deposit;
        Logger.debug("envelope.envelopeTypeLov : %d", envelope.envelopeTypeLov);
        Logger.debug("envelope.number: %s", envelope.envelopeNumber);
        for (LgEnvelopeContent l : envelope.envelopeContents) {
            if (l != null) {
                Logger.debug("content amount: %d", l.amount);
                Logger.debug("content content: %d", l.contentTypeLov);
                Logger.debug("content unit: %d", l.unitLov);
            }
        }
        renderArgs.put("confirm", true);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (!manager.envelopeDeposit()) {
            Logger.error("TODO ERROR HERE ???");
        }
        Logger.debug("------------ > Current Status %s", manager.getStatus().name());
        boolean done = false;
        while (!done) {
            Logger.debug("------------ > Current Status %s", manager.getStatus().name());
            switch (manager.getStatus()) {
                case IDLE:
                    done = true;
                    break;
                case ERROR:
                    Logger.error("TODO: ERROR DAVE HELP ME");
                    index();
                    return;
                default:
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                    }
                    break;
            }
        }
        // TODO: Put in a better place.
        for (LgEnvelopeContent c : envelope.envelopeContents) {
            c.envelope = envelope;
        }
        envelope.save();

        deposit.finishDate = new Date();
        deposit.save();

        flash.success("Deposit is done!");
        Application.index();
    }
}
