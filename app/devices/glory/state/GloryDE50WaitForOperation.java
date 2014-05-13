/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.glory.GloryDE50DeviceStateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.poll.GloryDE50Collect;
import devices.glory.state.poll.GloryDE50Count;
import devices.glory.state.poll.GloryDE50EnvelopeDeposit;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.glory.state.poll.GloryDE50Reset;
import devices.glory.state.poll.GloryDE50StoringErrorReset;
import devices.glory.task.GloryDE50TaskCollect;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.task.GloryDE50TaskEnvelopeDeposit;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.task.GloryDE50TaskOperation;
import devices.glory.task.GloryDE50TaskReset;
import devices.glory.task.GloryDE50TaskStoringReset;

/**
 *
 * @author adji
 */
public class GloryDE50WaitForOperation extends GloryDE50StateOperation {

    public GloryDE50WaitForOperation(GloryDE50DeviceStateApi api) {
        super(api);
    }

    public DeviceStateInterface call(GloryDE50TaskOperation task) {
        GloryDE50OperationResponse response = new GloryDE50OperationResponse();
        String err = api.sendGloryDE50Operation(task.getOperation(), task.isDebug(), response);
        if (err == null) {
            task.setError(null);
            task.setResponse(response);
        } else {
            task.setError(err);
            task.setResponse(null);
        }
        // Don't go to the error state anyway
        return null;
    }

    public DeviceStateInterface call(GloryDE50TaskCollect task) {
        task.setReturnValue(true);
        return new GloryDE50Collect(api);
    }

    public DeviceStateInterface call(GloryDE50TaskCount task) {
        task.setReturnValue(true);
        return new GloryDE50Count(api, task.getDesiredQuantity(), task.getCurrency());
    }

    public DeviceStateInterface call(GloryDE50TaskEnvelopeDeposit task) {
        task.setReturnValue(true);
        return new GloryDE50EnvelopeDeposit(api);
    }

    public DeviceStateInterface call(GloryDE50TaskReset task) {
        task.setReturnValue(true);
        return new GloryDE50Reset(api, new GloryDE50GotoNeutral(api));
    }

    public DeviceStateInterface call(GloryDE50TaskStoringReset task) {
        task.setReturnValue(true);
        return new GloryDE50StoringErrorReset(api);
    }

    public DeviceStateInterface call(DeviceTaskOpenPort task) {
        task.setReturnValue(true);
        return new GloryDE50OpenPort(api, task.getPort());
    }
}
