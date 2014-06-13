/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate;

/**
 *
 * @author adji
 */
public class Finish extends ModelFacadeSubStateAbstract {

    public Finish(ModelFacadeSubStateApi api) {
        super(api);
    }

    @Override
    public String getSubStateName() {
        return "FINISH";
    }

    @Override
    public String getNeededActionAction() {
        return "finish";
    }

    @Override
    public boolean canFinishAction() {
        return true;
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     // Was canceled is ok
     case CANCELING:
     break;
     // Ok, came here from previous state.
     case PUT_THE_BILLS_ON_THE_HOPER:
     break;
     default:
     Logger.debug("Finish invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
}
