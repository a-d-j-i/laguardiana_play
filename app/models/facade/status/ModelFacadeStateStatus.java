package models.facade.status;

import models.db.LgDeposit;
import models.facade.state.substate.ModelFacadeSubStateAbstract;
import play.Logger;

/**
 * // TODO: Implement this class correctly.
 * @author adji
 */
public class ModelFacadeStateStatus {

    private final String subStateName;
    private final String neededController;
    private final String neededAction;
    private final Object formData;
    private final String actionMessage;
    private final Integer depositId;
    private final boolean isError;

    public ModelFacadeStateStatus(String subStateName, String neededController, String neededAction, Object formData, String actionMessage, Integer depositId) {
        this.subStateName = subStateName;
        this.neededController = neededController;
        this.neededAction = neededAction;
        this.formData = formData;
        this.actionMessage = actionMessage;
        this.depositId = depositId;
        this.isError = false;
    }

    public ModelFacadeStateStatus(String neededController, ModelFacadeSubStateAbstract subState, Object formData, Integer depositId) {
        this(neededController, subState.getSubStateName(), subState.getNeededAction(), formData, subState.getMessage(null), depositId);
    }

    public ModelFacadeStateStatus(String neededController, ModelFacadeSubStateAbstract subState, Object formData) {
        this(neededController, subState.getSubStateName(), subState.getNeededAction(), formData, subState.getMessage(null), null);
    }

    public ModelFacadeStateStatus(String neededController, final String neededAction, final String subStateName) {
        this(neededController, subStateName, neededAction, null, null, null);
    }

    public LgDeposit getDeposit() {
        if (depositId == null) {
            Logger.error("getDeposit invalid depositId %d", depositId);
            return null;
        }
        return LgDeposit.findById(depositId);
    }

    public String getState() {
        return subStateName;
    }

    public String getNeededController() {
        return neededController;
    }

    public String getNeededAction() {
        return neededAction;
    }

    public Object getFormData() {
        return formData;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public Integer getDepositId() {
        return depositId;
    }

    public boolean isError() {
        return isError;
    }

}
