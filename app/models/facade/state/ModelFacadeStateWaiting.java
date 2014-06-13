package models.facade.state;

import models.ModelFacade;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
public class ModelFacadeStateWaiting extends ModelFacadeStateAbstract {

    public ModelFacadeStateWaiting(ModelFacade.ModelFacadeStateApi api) {
        super(api);
    }

    @Override
    public boolean startAction(ModelFacadeStateAbstract userAction) {
        api.setCurrentState(userAction);
        return true;
    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("Application", "Index", "IDLE");
    }

    @Override
    public ModelFacadeStateAbstract init() {
        return this;
    }

    @Override
    public boolean finish() {
        return false;
    }

}
