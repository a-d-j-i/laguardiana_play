package models.facade.state;

import models.ModelFacade;
import controllers.FilterController;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
public class ModelFacadeStateFilter extends ModelFacadeStateAbstract {

    final private FilterController.FilterData data;

    public ModelFacadeStateFilter(ModelFacade.ModelFacadeStateApi api, FilterController.FilterData data) {
        super(api);
        this.data = data;

    }

    @Override
    public ModelFacadeStateStatus getStatus() {
        return new ModelFacadeStateStatus("FilterController", subState, data);
    }

    @Override
    public ModelFacadeStateAbstract init() {
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

}
