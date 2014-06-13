 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate;

/**
 *
 * @author adji
 */
public class Jam extends ModelFacadeSubStateAbstract {

    final protected ModelFacadeSubStateAbstract prevState;

    public Jam(ModelFacadeSubStateAbstract prevState, ModelFacadeSubStateApi api) {
        super(api);
        this.prevState = prevState;
    }

    @Override
    public String getSubStateName() {
        return "JAM";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     switch (m.getState()) {
     case REMOVE_REJECTED_BILLS:
     stateApi.setState(new RemoveRejectedBills(stateApi, this));
     break;
     case JAM:
     break;
     case NEUTRAL:
     stateApi.setState(prevState);
     break;
     default:
     Logger.debug("JAM onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
}
