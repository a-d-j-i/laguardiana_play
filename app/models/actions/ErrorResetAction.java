/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.EnumMap;
import models.actions.states.ResetState;

/**
 *
 * @author adji
 */
public class ErrorResetAction extends UserAction {

    static final EnumMap<GloryManager.Status, String> messageMap = new EnumMap<GloryManager.Status, String>(GloryManager.Status.class);

    static {
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(GloryManager.Status.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(GloryManager.Status.ERROR, "application.error");
    }

    public ErrorResetAction() {
        super(null, null, messageMap);
        state = new ResetState(new StateApi());
    }

    @Override
    public String getNeededAction() {
        return "counterError";
    }

    @Override
    final public String getNeededController() {
        return "Application";
    }

    @Override
    public void start() {
        userActionApi.resetGlory();
    }
}
