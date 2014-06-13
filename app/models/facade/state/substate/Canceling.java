/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate;

/**
 *
 * @author adji
 */
public class Canceling extends ModelFacadeSubStateAbstract {

    public Canceling(ModelFacadeSubStateApi api) {
        super(api);
    }

    @Override
    public String getSubStateName() {
        return "CANCELING";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case NEUTRAL:
     stateApi.setState(new Finish(stateApi));
     break;
     case INITIALIZING:
     break;
     case CANCELING:
     break;
     case REMOVE_REJECTED_BILLS:
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     break;
     case REMOVE_THE_BILLS_FROM_HOPER:
     break;
     case JAM: //The door is jamed.
     stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
     break;
     default:
     Logger.debug("Canceling invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
}
