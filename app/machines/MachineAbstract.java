package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.DeviceEventListener;
import devices.device.status.DeviceStatusInterface;
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
import models.events.MachineEvent;
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
                if (!evt.getStatus().dontLog()) {
                    MachineEvent.save(MachineAbstract.this, evt.toString());
                }
                submit(new MachineJob<Void>(MachineAbstract.this) {
                    @Override
                    public Void doJobWithResult() {
                        MachineAbstract.this.onDeviceEvent(d, evt.getStatus());
                        return null;
                    }
                });
            }
        });
    }

    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface status) {
        currentState.onDeviceEvent(dev, status);
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
        MachineEvent.save(this, "Machine Start start");
        // start devices.
        for (DeviceInterface d : deviceMap.values()) {
            MachineEvent.save(this, "Start device %s", d.toString());
            d.start();
            MachineEvent.save(this, "Start device %s done", d.toString());
        }
    }

    @Override
    public void stop() {
        for (DeviceInterface d : deviceMap.values()) {
            MachineEvent.save(this, "Stop device %s", d.toString());
            d.stop();
            MachineEvent.save(this, "Stop device %s done", d.toString());
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

    public MachineStatus getStatus() {
        return currentState.getStatus();
    }

    public boolean onAcceptDepositEvent() {
        MachineEvent.save(this, "onAcceptDepositEvent");
        return currentState.onAcceptDepositEvent();
    }

    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refDeposit) {
        MachineEvent.save(this, "onStartEnvelopeDeposit");
        return currentState.onStartEnvelopeDeposit(refDeposit);
    }

    public boolean onStartBillDeposit(BillDeposit refDeposit) {
        MachineEvent.save(this, "onStartBillDeposit");
        return currentState.onStartBillDeposit(refDeposit);
    }

    public boolean onConfirmDeposit() {
        MachineEvent.save(this, "onConfirmDeposit");
        return currentState.onConfirmDepositEvent();
    }

    public boolean onCancelDeposit() {
        MachineEvent.save(this, "onCancelDeposit");
        return currentState.onCancelDepositEvent();
    }

    public boolean onReset() {
        MachineEvent.save(this, "onReset");
        return currentState.onReset();
    }

    public boolean onStoringErrorReset() {
        MachineEvent.save(this, "onStoringErrorReset");
        return currentState.onStoringErrorReset();
    }

}
