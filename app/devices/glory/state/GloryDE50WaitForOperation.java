/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskCollect;
import devices.device.task.DeviceTaskCount;
import devices.device.task.DeviceTaskEnvelopeDeposit;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStoringErrorReset;
import devices.glory.GloryDE50Device;
import devices.glory.state.poll.GloryDE50Collect;
import devices.glory.state.poll.GloryDE50Count;
import devices.glory.state.poll.GloryDE50EnvelopeDeposit;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.glory.state.poll.GloryDE50Reset;
import devices.glory.state.poll.GloryDE50StoringErrorReset;
import devices.glory.task.GloryDE50TaskCount;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50WaitForOperation extends GloryDE50StateOperation {

    public GloryDE50WaitForOperation(GloryDE50Device api) {
        super(api);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskCollect) {
            task.setReturnValue(true);
            return new GloryDE50Collect(api);
        } else if (task instanceof DeviceTaskCount) {
            task.setReturnValue(true);
            if (task instanceof GloryDE50TaskCount) {
                GloryDE50TaskCount count = (GloryDE50TaskCount) task;
                return new GloryDE50Count(api, count.getDesiredQuantity(), count.getCurrency());
            } else {
                return new GloryDE50Count(api, null, 1);
            }
        } else if (task instanceof DeviceTaskEnvelopeDeposit) {
            task.setReturnValue(true);
            return new GloryDE50EnvelopeDeposit(api);
        } else if (task instanceof DeviceTaskReset) {
            task.setReturnValue(true);
            return new GloryDE50Reset(api, new GloryDE50GotoNeutral(api));
        } else if (task instanceof DeviceTaskStoringErrorReset) {
            task.setReturnValue(true);
            return new GloryDE50StoringErrorReset(api);
        } else if (task instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
            if (api.open(open.getPort())) {
                Logger.debug("GloryDE50WaitForOperation new port %s", open.getPort());
                task.setReturnValue(true);
                return this;
            } else {
                Logger.debug("GloryDE50WaitForOperation new port %s failed to open", open.getPort());
                task.setReturnValue(false);
                return new GloryDE50OpenPort(api);
            }
        }
        // support operations.
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50WaitForOperation";
    }

}
