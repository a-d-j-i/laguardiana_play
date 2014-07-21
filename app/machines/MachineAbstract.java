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
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateInterface;
import machines.status.MachineStatus;
import models.db.LgUser;
import models.lov.Currency;
import play.Logger;

/**
 * Composite of devices.
 *
 * @author adji
 */
abstract public class MachineAbstract implements MachineInterface, MachineStateApiInterface {

    final private Map<Integer, MachineDeviceDecorator> deviceMap = new HashMap<Integer, MachineDeviceDecorator>();
    protected MachineStateInterface currentState;

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

    public MachineStatus getStatus() {
        return currentState.getStatus();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    abstract public boolean isBagReady();

    abstract public boolean isBagFull();

    abstract public boolean count(Currency currency);

    public boolean onAcceptDepositEvent() {
        return currentState.onAcceptDepositEvent();
    }

    public boolean onStartEnvelopeDeposit(LgUser user, String userCode, Integer userCodeLovId) {
        return currentState.onStartEnvelopeDeposit(user, userCode, userCodeLovId);
    }

    public boolean onStartBillDeposit(LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        return currentState.onStartBillDeposit(user, currency, userCode, userCodeLovId);
    }

    public boolean onConfirmDeposit() {
        return currentState.onConfirmDepositEvent();
    }

    public boolean onCancelDeposit() {
        return currentState.onCancelDepositEvent();
    }

}
