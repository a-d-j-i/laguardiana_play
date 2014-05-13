package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceCommandInterface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskInterface;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.task.GloryDE50TaskCollect;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.task.GloryDE50TaskEnvelopeDeposit;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.task.GloryDE50TaskOperation;
import devices.glory.task.GloryDE50TaskReset;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import devices.glory.task.GloryDE50TaskWithdraw;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import machines.Machine;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract implements DeviceClassCounterIntreface {

    final GloryDE50DeviceStateApi api;

    public GloryDE50Device(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc, new SynchronousQueue<DeviceTaskInterface>());
        api = new GloryDE50DeviceStateApi(getOperationQueue());
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskInterface deviceTask = new DeviceTaskOpenPort(value);
            if (submit(deviceTask)) {
                return deviceTask.get();
            }
            return false;
        }
        return false;
    }

    private String initialPortValue;

    @Override
    protected void initDeviceProperties() {
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        initialPortValue = lgSerialPort.value;
    }

    @Override
    public DeviceStateInterface init() {
        return new GloryDE50OpenPort(api, initialPortValue);
    }

    @Override
    public void finish() {
        Logger.debug("Executing GotoNeutral command on Stop");
        api.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    enum GloryDE50SimpleTask implements DeviceCommandInterface {

        TASK_COLLECT {
                    @Override
                    public DeviceTaskAbstract getTask() {
                        return new DeviceTaskAbstract() {
                        };
                    }

                },
        TASK_ENVELOPE_DEPOSIT {
                    @Override
                    public DeviceTaskAbstract getTask() {
                        return new DeviceTaskAbstract() {
                        };
                    }

                },
        TASK_STORE_DEPOSIT {
                    @Override
                    public DeviceTaskAbstract getTask() {
                        return new DeviceTaskAbstract() {
                        };
                    }

                };
    }

    public boolean count(final Map<Integer, Integer> desiredQuantity, final Integer currency) {
        DeviceTaskInterface deviceTask = new GloryDE50TaskCount(desiredQuantity, currency);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean envelopeDeposit() {
        DeviceTaskInterface deviceTask = new GloryDE50TaskEnvelopeDeposit();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean collect() {
        DeviceTaskInterface deviceTask = new GloryDE50TaskCollect();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean reset() {
        DeviceTaskInterface deviceTask = new GloryDE50TaskReset();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean storingErrorReset() {
        DeviceTaskInterface deviceTask = new GloryDE50TaskReset();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        DeviceTaskInterface deviceTask = new GloryDE50TaskStoreDeposit();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean withdrawDeposit() {
        DeviceTaskInterface deviceTask = new GloryDE50TaskWithdraw();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public GloryDE50TaskOperation sendGloryDE50Operation(OperationWithAckResponse c, boolean b) {
        GloryDE50TaskOperation deviceTask = new GloryDE50TaskOperation(c, b);
        if (submit(deviceTask)) {
            if (deviceTask.get()) {
                return deviceTask;
            }
        }
        return null;
    }

    @Override
    public GloryDE50StateAbstract getCurrentState() {
        return (GloryDE50StateAbstract) super.getCurrentState();
    }

    public boolean cancelDeposit() {
        return getCurrentState().cancelDeposit();
    }

    public Integer getCurrency() {
        return getCurrentState().getCurrency();
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return getCurrentState().getCurrentQuantity();
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return getCurrentState().getDesiredQuantity();
    }

    public boolean clearError() {
        return getCurrentState().clearError();
    }

    @Override
    public DeviceStatus getStatus() {
        GloryDE50StateAbstract st = getCurrentState();
        return new DeviceStatus(st.getError());
    }

}
