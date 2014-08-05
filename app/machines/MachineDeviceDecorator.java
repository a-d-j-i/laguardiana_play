package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.DeviceEventListener;
import devices.device.task.DeviceTaskAbstract;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import models.db.LgBillType;
import models.db.LgDevice;
import models.db.LgDevice.DeviceType;
import models.db.LgDeviceProperty;
import models.db.LgDeviceSlot;
import play.Logger;

/**
 *
 * @author adji
 */
public class MachineDeviceDecorator implements DeviceInterface {

    final protected LgDevice lgd;
    final private DeviceInterface device;

    public MachineDeviceDecorator(String machineDeviceId, DeviceType deviceType, DeviceInterface device) {
        this.device = device;
        lgd = LgDevice.getOrCreateByMachineId(machineDeviceId, deviceType);
        this.device.addEventListener(new DeviceEventListener() {

            public void onDeviceEvent(DeviceEvent evt) {
                eventHistory.offer(evt);
                if (evt.getSource() == MachineDeviceDecorator.this.device) {
                    final DeviceEvent le = new DeviceEvent(MachineDeviceDecorator.this, evt.getStatus());
                    for (DeviceEventListener counterListener : listeners) {
                        counterListener.onDeviceEvent(le);
                    }
                } else {
                    Logger.error("Ignoring event %s because is not from the decorated device", evt.toString());
                }
            }
        });
        for (String s : device.getNeededProperties()) {
            LgDeviceProperty.getOrCreateProperty(lgd, s, LgDeviceProperty.EditType.STRING);
        }
    }

    public Integer getDeviceId() {
        return lgd.deviceId;
    }

    public String getMachineDeviceId() {
        return lgd.machineDeviceId;
    }

    public DeviceType getType() {
        return lgd.deviceType;
    }

    public void start() {
        device.start();
        // set needed properties, must be done after device.start. TODO: Fix.
        for (String s : device.getNeededProperties()) {
            LgDeviceProperty prop = LgDeviceProperty.getOrCreateProperty(lgd, s, LgDeviceProperty.EditType.STRING);
            device.setProperty(s, prop.value);
        }
    }

    public void stop() {
        device.stop();
    }

    private final Set<DeviceEventListener> listeners = new HashSet<DeviceEventListener>();
    //Queue<DeviceEvent> events = new LinkedList<CounterEscrowFullEvent>();

    public void addEventListener(DeviceEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(DeviceEventListener listener) {
        this.listeners.remove(listener);
    }

    // Just for logging proposes.
    final private BlockingQueue<DeviceEvent> eventHistory = new ArrayBlockingQueue<DeviceEvent>(10);

    public DeviceEvent getLastEvent() {
        return eventHistory.poll();
    }

    public Future<Boolean> submit(DeviceTaskAbstract deviceTask) {
        return device.submit(deviceTask);
    }

    public boolean submitSynchronous(final DeviceTaskAbstract deviceTask) {
        return device.submitSynchronous(deviceTask);
    }

    public List<LgDeviceProperty> getEditableProperties() {
        return LgDeviceProperty.getEditables(lgd);
    }

    public boolean setProperty(String property, String value) {
        LgDeviceProperty l = LgDeviceProperty.getOrCreateProperty(lgd, property, LgDeviceProperty.EditType.STRING);
        if (l != null) {
            if (device.setProperty(property, value)) {
                l.value = value;
                l.save();
                return true;
            }
        }
        return false;
    }

    public LgDeviceProperty getProperty(String property) {
        return LgDeviceProperty.getProperty(lgd, property);
    }

    public List<String> getNeededProperties() {
        return device.getNeededProperties();
    }

    @Override
    public String toString() {
        return "Device : " + lgd.machineDeviceId;
    }

    public LgDevice getLgDevice() {
        return lgd;
    }

    public Map<LgBillType, Integer> getQuantities(Map<String, Integer> currentQuantity) {
        Map<LgBillType, Integer> ret = new HashMap<LgBillType, Integer>();
        for (String slot : currentQuantity.keySet()) {
            LgDeviceSlot l = LgDeviceSlot.find(lgd, slot);
            if (l == null) {
                Logger.error("Configuration problem, slot %s not found for device %s", slot, lgd.toString());
            } else {
                Integer oldQ = ret.get(l.billType);
                if (oldQ == null) {
                    oldQ = 0;
                }
                oldQ += currentQuantity.get(slot);
                ret.put(l.billType, oldQ);
            }
        }
        return ret;
    }

}
