package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.Bill;
import models.Deposit;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.lov.DepositUserCodeReference;
import models.lov.EnvelopeType;
import play.Logger;
import play.mvc.With;

@With( Secure.class)
public class EnvelopeDepositController extends DepositController {

    public static void index() {
        Application.index();
    }

    public static void inputReference(String reference1, String reference2) throws Throwable {
        //TODO: Validate references depending on system properties. 
        Deposit d = DepositController.createDeposit(reference1, reference2);
        if (d != null) {
            getEnvelopeContents(Integer.toString(d.depositId), null);
            return;
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
        manager.envelopeDeposit();
        boolean done = false;
        while (!done) {
            Logger.debug("Current Status %s", manager.getStatus().name());
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


        if (manager.getStatus() != Manager.Status.READY_TO_STORE) {
            Logger.debug("NOT READY TO STORE");
            index();
            return;
        }
        if (!manager.storeDeposit(Integer.parseInt(depositId))) {
            Logger.error("TODO: ERROR DAVE HELP ME");
            index();
            return;
        }

        done = false;
        while (!done) {
            Logger.debug("Current Status %s", manager.getStatus().name());
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
        envelope.save();

        flash.success("Deposit is done!");
        render(deposit);
    }
// TODO: Finish

    public static void cancelDeposit(String depositId) {
        //TODO: Check if there are batches related to this deposit.
        // infrom and send cancelDeposit
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();

        manager.cancelDeposit();
        boolean done = false;
        while (!done) {
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
        finishDeposit(depositId);
    }

    public static void finishDeposit(String depositId) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        deposit.finishDate = new Date();
        deposit.save();
        Application.index();
    }
}
