package machines.P500_GloryDE50.states;

import devices.device.status.DeviceStatusInterface;
import devices.glory.status.GloryDE50Status;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.context.P500GloryDE50StateContext;
import machines.states.*;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateError extends MachineStateAbstract {

    protected final String error;
    protected final MachineStateAbstract prevState;
    protected final P500GloryDE50StateContext context;

    public P500GloryDE50StateError(P500GloryDE50StateContext context, MachineStateAbstract prevState, String error, Object... args) {
        this.error = String.format(error, args);
        this.context = context;
        this.prevState = prevState;
        Logger.error("-----------------> MACHINE ERROR : %s", error);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(null, "ErrorController.onError", "ERROR", error);
    }

    @Override
    public boolean onReset() {
        return context.reset();
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {

        if (st.is(GloryDE50Status.GloryDE50StatusType.NEUTRAL)) {
            context.setCurrentState(prevState);
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateError{" + "error=" + error + ", prevState=" + prevState + ", context=" + context + '}';
    }

}
