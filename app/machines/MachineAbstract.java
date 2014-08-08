package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.DeviceEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import machines.jobs.MachineJob;
import machines.states.MachineStateInterface;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.EnvelopeDeposit;
import play.Logger;

/**
 * Composite of devices.
 *
 * @author adji
 */
abstract public class MachineAbstract implements MachineInterface {

    final private Map<Integer, MachineDeviceDecorator> deviceMap = new HashMap<Integer, MachineDeviceDecorator>();
    private MachineStateInterface currentState;

    protected void addDevice(final MachineDeviceDecorator d) {
        deviceMap.put(d.getDeviceId(), d);
        d.addEventListener(new DeviceEventListener() {

            public void onDeviceEvent(final DeviceEvent evt) {
                // don't wait.
                submit(new MachineJob<Void>(MachineAbstract.this) {
                    @Override
                    public Void doJobWithResult() {
                        currentState.onDeviceEvent(d, evt.getStatus());
                        return null;
                    }
                });
            }
        });
    }

    public List<MachineDeviceDecorator> getDevices() {
        return new ArrayList<MachineDeviceDecorator>(deviceMap.values());
    }

    public MachineDeviceDecorator findDeviceById(Integer deviceId) {
        return deviceMap.get(deviceId);
    }

    public Future submit(MachineJob job) {
        return job.now();
    }

    public <V> V execute(MachineJob<V> job) {
        return job.runNow();
    }

    @Override
    public void start() {
        Logger.debug("Machine Start start");
        // start devices.
        for (DeviceInterface d : deviceMap.values()) {
            Logger.debug("Start device %s", d.toString());
            d.start();
            Logger.debug("Start device %s done", d.toString());
        }
    }

    @Override
    public void stop() {
        for (DeviceInterface d : deviceMap.values()) {
            Logger.debug("Stop device %s", d.toString());
            d.stop();
            Logger.debug("Stop device %s done", d.toString());
        }
    }

    public boolean setCurrentState(MachineStateInterface state) {
        Logger.debug("setCurrentState calling state %s onStart", state.toString());
        if (!state.onStart()) {
            Logger.error("Rejecting state %s leave in state %s", state.toString(), currentState.toString());
            return false;
        }
        Logger.debug("setCurrentState setting current state to : %s old state %s", state.toString(),
                currentState == null ? "null" : currentState.toString());
        currentState = state;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    abstract public boolean isBagReady();

    abstract public boolean isBagFull();

    public MachineStatus getStatus() {
        return currentState.getStatus();
    }

    public boolean onAcceptDepositEvent() {
        return currentState.onAcceptDepositEvent();
    }

    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refDeposit) {
        return currentState.onStartEnvelopeDeposit(refDeposit);
    }

    public boolean onStartBillDeposit(BillDeposit refDeposit) {
        return currentState.onStartBillDeposit(refDeposit);
    }

    public boolean onConfirmDeposit() {
        return currentState.onConfirmDepositEvent();
    }

    public boolean onCancelDeposit() {
        return currentState.onCancelDepositEvent();
    }

    public boolean onReset() {
        return currentState.onReset();
    }

    public boolean onStoringErrorReset() {
        return currentState.onStoringErrorReset();
    }

}
