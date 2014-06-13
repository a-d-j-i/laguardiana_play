package models.facade.state.substate;

/**
 *
 * @author adji
 */
public class WaitForClosedGate extends ModelFacadeSubStateAbstract {

    protected final ModelFacadeSubStateAbstract nextAction;

    public WaitForClosedGate(ModelFacadeSubStateAbstract nextAction, ModelFacadeSubStateApi api) {
        super(api);
        this.nextAction = nextAction;
    }

    @Override
    public String getSubStateName() {
        return "STORING";
    }
    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.error("ActionState invalid onGloryEvent %s", m.toString());
     }
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreShutter()) {
     switch (status.getShutterState()) {
     case SHUTTER_CLOSED:
     stateApi.setState(nextAction);
     break;
     case SHUTTER_OPEN:
     break;
     default:
     Logger.error("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name());
     break;
     }
     }
     }
     */
}
