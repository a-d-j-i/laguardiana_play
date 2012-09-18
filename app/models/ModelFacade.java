/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.actions.UserAction;
import models.actions.UserAction.CurrentStep;
import models.db.LgBillType;
import play.Logger;
import validation.Bill;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 *
 * @author aweil
 */
public class ModelFacade {
    
    static public enum CurrentState {

        RESERVED,
        ERROR,
        IDLE,
        RUNNING,
        FINISH;
    }
    // FACTORY

    //public final F.EventStream event = new F.EventStream();

    
    
    private static UserAction currentAction = null;
    final static private GloryEvent gloryEvent = new GloryEvent();
    final static private Manager.ControllerApi manager = CounterFactory.getGloryManager();

    static protected class GloryEvent implements Runnable {

        public void run() {
            // TODO: Pass this to the orchestrator as an event.
            if (currentAction == null) {
                Logger.error("GloryEvent current user action is null");
            } else {
                currentAction.gloryDone(manager.getStatus(), manager.getErrorDetail());
            }
        }
    }

    static public class ActionApi {

        public boolean count(Integer numericId) {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentAction == null) {
                    Logger.error("count currentAction is null");
                    return false;
                }
                return manager.count(gloryEvent, null, numericId);
            }
        }

        public boolean storeDeposit(Integer depositId) {
            return manager.storeDeposit(depositId);
        }

        public boolean cancelDeposit() {
            return manager.cancelDeposit(gloryEvent);
        }

        public List<Bill> getCurrentCounters(Integer currency) {
            List<Bill> ret = new ArrayList<Bill>();
            List<LgBillType> billTypes = LgBillType.find(currency);


            Map<Integer, Integer> desiredQuantity = null;
            Map<Integer, Integer> currentQuantity = null;
            if (manager != null) {
                currentQuantity = manager.getCurrentQuantity();
                desiredQuantity = manager.getDesiredQuantity();
            }
            for (LgBillType bb : billTypes) {
                Bill b = new Bill();
                b.billType = bb;
                b.tid = bb.billTypeId;
                b.btd = bb.toString();
                b.d = bb.denomination;
                if (currentQuantity != null && currentQuantity.containsKey(bb.slot)) {
                    b.q = currentQuantity.get(bb.slot);
                }
                if (desiredQuantity != null && desiredQuantity.containsKey(bb.slot)) {
                    b.dq = desiredQuantity.get(bb.slot);
                }
                ret.add(b);
            }
            return ret;
        }
    }

    synchronized static public UserAction getCurrentAction() {
        return ModelFacade.currentAction;
    }

    synchronized static public void startAction(UserAction userAction) {
        if (ModelFacade.currentAction != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        ModelFacade.currentAction = userAction;
        ModelFacade.currentAction.start(new ActionApi());
    }

    synchronized public static void finishAction() {
        if (ModelFacade.currentAction == null) {
            Logger.error("finishDeposit currentAction is null");
            return;
        }
        CurrentStep c = ModelFacade.currentAction.getCurrentStep();
        if (c != CurrentStep.FINISH) {
            Logger.error("finishDeposit Invalid step %s", c.name());
            return;
        }
        ModelFacade.currentAction = null;
    }
}
