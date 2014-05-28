/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.task.GloryDE50TaskCount;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.poll.GloryDE50Collect;
import devices.glory.state.poll.GloryDE50Count;
import devices.glory.state.poll.GloryDE50EnvelopeDeposit;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.glory.state.poll.GloryDE50Reset;
import devices.glory.state.poll.GloryDE50StoringErrorReset;
import devices.glory.GloryDE50Device.GloryDE50TaskType;
import devices.glory.task.GloryDE50TaskOperation;

/**
 *
 * @author adji
 */
public class GloryDE50WaitForOperation extends GloryDE50StateOperation {

    public GloryDE50WaitForOperation(GloryDE50DeviceStateApi api) {
        super(api);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        DeviceTaskAbstract task = (DeviceTaskAbstract) t;
        switch ((GloryDE50TaskType) task.getType()) {
            case TASK_COLLECT:
                task.setReturnValue(true);
                return new GloryDE50Collect(api);
            case TASK_COUNT:
                task.setReturnValue(true);
                GloryDE50TaskCount count = (GloryDE50TaskCount) task;
                return new GloryDE50Count(api, count.getDesiredQuantity(), count.getCurrency());
            case TASK_ENVELOPE_DEPOSIT:
                task.setReturnValue(true);
                return new GloryDE50EnvelopeDeposit(api);
            case TASK_RESET:
                task.setReturnValue(true);
                return new GloryDE50Reset(api, new GloryDE50GotoNeutral(api));
            case TASK_STORING_ERROR_RESET:
                task.setReturnValue(true);
                return new GloryDE50StoringErrorReset(api);
            case TASK_OPERATION:
                GloryDE50TaskOperation op = (GloryDE50TaskOperation) task;
                GloryDE50OperationResponse response = new GloryDE50OperationResponse();
                String err = api.sendGloryDE50Operation(op.getOperation(), op.isDebug(), response);
                if (err == null) {
                    op.setError(null);
                    op.setResponse(response);
                } else {
                    op.setError(err);
                    op.setResponse(null);
                }
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                return new GloryDE50OpenPort(api, open.getPort());
            case TASK_WITHDRAW_DEPOSIT:
            case TASK_STORE_DEPOSIT:
            default:
                return null;
        }
    }

}
