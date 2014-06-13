package models.facade.state;

import models.ModelFacade;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
public class ModelFacadeStateStroringError extends ModelFacadeStateError {

    public ModelFacadeStateStroringError(ModelFacade.ModelFacadeStateApi api, String error) {
        super(api, error);
    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("ErrorController", "onStoringError", "ERROR");
    }

    @Override
    public boolean finish() {
        if (api.getMachine().storingErrorReset()) {
            api.setCurrentState(new ModelFacadeStateWaiting(api));
        }
        return true;
    }
}
