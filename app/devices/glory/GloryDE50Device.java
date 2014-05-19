package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.glory.state.GloryDE50StateAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.task.GloryDE50TaskOperation;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import java.util.List;
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

    public enum GloryDE50TaskType {

        TASK_OPEN_PORT,
        TASK_COUNT,
        TASK_COLLECT,
        TASK_ENVELOPE_DEPOSIT,
        TASK_RESET,
        TASK_STORING_ERROR_RESET,
        TASK_STORE_DEPOSIT,
        TASK_WITHDRAW_DEPOSIT,
        TASK_OPERATION;
    }
    final GloryDE50DeviceStateApi api;

    public GloryDE50Device(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc, new SynchronousQueue<DeviceTaskAbstract>());
        api = new GloryDE50DeviceStateApi(operationQueue);
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(GloryDE50TaskType.TASK_OPEN_PORT, value);
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

    private boolean runSimpleTask(GloryDE50TaskType st) {
        DeviceTaskAbstract deviceTask = new DeviceTaskAbstract(st);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean count(List<Integer> slotList) {
        return false;
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskCount(GloryDE50TaskType.TASK_COUNT, desiredQuantity, currency);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean envelopeDeposit() {
        return runSimpleTask(GloryDE50TaskType.TASK_ENVELOPE_DEPOSIT);
    }

    public boolean collect() {
        return runSimpleTask(GloryDE50TaskType.TASK_COLLECT);
    }

    public boolean reset() {
        return runSimpleTask(GloryDE50TaskType.TASK_RESET);
    }

    public boolean storingErrorReset() {
        return runSimpleTask(GloryDE50TaskType.TASK_STORING_ERROR_RESET);
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskStoreDeposit(GloryDE50TaskType.TASK_COUNT, sequenceNumber);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean withdrawDeposit() {
        return runSimpleTask(GloryDE50TaskType.TASK_WITHDRAW_DEPOSIT);
    }

    public GloryDE50TaskOperation sendGloryDE50Operation(OperationWithAckResponse c, boolean b) {
        GloryDE50TaskOperation deviceTask = new GloryDE50TaskOperation(GloryDE50TaskType.TASK_OPERATION, c, b);
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
