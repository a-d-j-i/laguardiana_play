package models.facade.state.substate;

/**
 *
 * @author adji
 */
public class RemoveRejectedBills extends ModelFacadeSubStateAbstract {

    final protected ModelFacadeSubStateAbstract prevState;

    public RemoveRejectedBills(ModelFacadeSubStateAbstract prevState, ModelFacadeSubStateApi api) {
        super(api);
        this.prevState = prevState;
    }

    @Override
    public String getSubStateName() {
        return "REMOVE_REJECTED_BILLS";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     switch (m.getState()) {
     // Envelope deposit.
     case REMOVE_REJECTED_BILLS:
     break;
     case JAM:
     stateApi.setState(new Jam(stateApi, this));
     break;
     default:
     stateApi.setState(prevState);
     break;
     }
     }
     */
}
