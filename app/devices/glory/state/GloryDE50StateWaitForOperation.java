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
import devices.glory.state.poll.GloryDE50StateCollect;
import devices.glory.state.poll.GloryDE50StateCount;
import devices.glory.state.poll.GloryDE50StateEnvelopeDeposit;
import devices.glory.state.poll.GloryDE50StateGotoNeutral;
import devices.glory.state.poll.GloryDE50StateReset;
import devices.glory.state.poll.GloryDE50StateStoringErrorReset;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.task.GloryDE50TaskOperation;
import java.util.HashMap;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateWaitForOperation extends GloryDE50StateAbstract {

    public GloryDE50StateWaitForOperation(GloryDE50Device api) {
        super(api);
    }

    @Override
    public DeviceStateInterface init() {
        return new GloryDE50StateGotoNeutral(api, this, false, true);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskCollect) {
            task.setReturnValue(true);
            return new GloryDE50StateCollect(api);
        } else if (task instanceof DeviceTaskCount) {
            task.setReturnValue(true);
            if (task instanceof GloryDE50TaskCount) {
                GloryDE50TaskCount count = (GloryDE50TaskCount) task;
                return new GloryDE50StateCount(api, count.getDesiredQuantity(), count.getCurrency());
            } else {
                return new GloryDE50StateCount(api, new HashMap<String, Integer>(), 1);
            }
        } else if (task instanceof DeviceTaskEnvelopeDeposit) {
            task.setReturnValue(true);
            return new GloryDE50StateEnvelopeDeposit(api);
        } else if (task instanceof DeviceTaskReset) {
            task.setReturnValue(true);
            return new GloryDE50StateReset(api, this);
        } else if (task instanceof DeviceTaskStoringErrorReset) {
            task.setReturnValue(true);
            return new GloryDE50StateStoringErrorReset(api);
        } else if (task instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
            if (api.open(open.getPort())) {
                Logger.debug("GloryDE50WaitForOperation new port %s", open.getPort());
                task.setReturnValue(true);
                return this;
            } else {
                Logger.debug("GloryDE50WaitForOperation new port %s failed to open", open.getPort());
                task.setReturnValue(false);
                return new GloryDE50StateOpenPort(api);
            }
        } else if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation opt = (GloryDE50TaskOperation) task;
            String err = api.writeOperation(opt, true);
            if (err != null) {
                opt.setError(err);
                task.setReturnValue(false);
                return null;
            }
            return new GloryDE50StateWaitForResponse(api, opt, this);
        }
        // support operations.
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateWaitForOperation";
    }

}
