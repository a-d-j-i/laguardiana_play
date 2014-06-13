package models.facade.state;

import models.ModelFacade;
import controllers.CountController;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
public class ModelFacadeStateCount extends ModelFacadeStateAbstract {

    final private CountController.CountData data;

    public ModelFacadeStateCount(ModelFacade.ModelFacadeStateApi api, CountController.CountData data) {
        super(api);
        this.data = data;

    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("CountController", subState, data);
    }

    @Override
    public ModelFacadeStateAbstract init() {
        api.getMachine().count(data.currency.currency.numericId, null);
        return this;
    }

    @Override
    public boolean finish() {
        if (api.getMachine().cancel()) {
            api.setCurrentState(new ModelFacadeStateWaiting(api));
        } else {
            api.setCurrentState(new ModelFacadeStateError(api, "Can't cancel"));
        }
        return true;
    }

    @Override
    public String toString() {
        return "ModelFacadeStateCounting";
    }

}
