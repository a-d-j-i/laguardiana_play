/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import static devices.glory.GloryDE50Device.STATUS.STORING;
import play.Logger;

/**
 *
 * @author adji
 */
public class WaitForStoreCommand extends GloryDE50StateAbstract {

    final GloryDE50StateAbstract prevStep;
    boolean sended = false;

    public WaitForStoreCommand(GloryDE50StateMachineApi api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public GloryDE50StateAbstract step() {
        Logger.debug("ReadyForCommand");
//        if (commandData.needToStoreDeposit()) {

        GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.operation.StoringStart(0));
        if (sret != null) {
            return sret;
        }
        api.notifyListeners(STORING);
        return prevStep;
    }
}
