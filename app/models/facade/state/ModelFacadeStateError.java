package models.facade.state;

import models.ModelFacade;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
public class ModelFacadeStateError extends ModelFacadeStateAbstract {

    final String error;

    public ModelFacadeStateError(ModelFacade.ModelFacadeStateApi api, String error) {
        super(api);
        this.error = error;
    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("ErrorController", "onError", "ERROR");
    }

    @Override
    public ModelFacadeStateAbstract init() {
        return this;
    }

    @Override
    public boolean finish() {
        if (api.getMachine().errorReset()) {
            api.setCurrentState(new ModelFacadeStateWaiting(api));
        }
        return true;
    }

}
