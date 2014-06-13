package machines;

import devices.device.DeviceInterface;
import devices.device.events.DeviceEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import machines.events.MachineEvent;
import machines.events.MachineEventListener;
import machines.status.MachineStatus;
import models.db.LgDeviceSlot;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class Machine implements DeviceEventListener {

    public DeviceInterface findDeviceById(Integer deviceId) {
        return deviceMap.get(deviceId);
    }

    public List<DeviceInterface> getDevices() {
        return new ArrayList<DeviceInterface>(deviceMap.values());
    }
    private final Set<MachineEventListener> listeners = new HashSet<MachineEventListener>();
    //Queue<MachineEvent> events = new LinkedList<CounterEscrowFullEvent>();

    synchronized public void addEventListener(MachineEventListener listener) {
        this.listeners.add(listener);
    }

    synchronized public void removeEventListener(MachineEventListener listener) {
        this.listeners.remove(listener);
    }

    // Just for logging proposes.
    final private BlockingQueue<MachineEvent> eventHistory = new ArrayBlockingQueue<MachineEvent>(10);

    synchronized protected void notifyListeners(MachineStatus status) {
        final MachineEvent le = new MachineEvent(this, status);
        eventHistory.offer(le);
        for (MachineEventListener counterListener : listeners) {
            counterListener.onMachineEvent(le);
        }
    }

    public MachineEvent getLastEvent() {
        return eventHistory.poll();
    }

    private final Map<Integer, DeviceInterface> deviceMap = new HashMap<Integer, DeviceInterface>();

    abstract protected List<DeviceInterface> getDeviceList();

    public void start() {
        List<DeviceInterface> devices = getDeviceList();
        if (devices == null) {
            throw new IllegalArgumentException("Machine must be reconfigured correctly");
        }
        for (DeviceInterface dev : devices) {
            if (dev == null) {
                throw new IllegalArgumentException("Machine must be reconfigured correctly");
            }
            Logger.debug("Start device %s", dev);
            dev.addEventListener(this);
            dev.start();
            deviceMap.put(dev.getDeviceId(), dev);
            Logger.debug("Start device %s done", dev);
        }
    }

    public void stop() {
        for (DeviceInterface d : deviceMap.values()) {
            Logger.debug("Stop device %s", d.toString());
            d.stop();
            Logger.debug("Stop device %s done", d.toString());
        }
    }

    abstract public boolean isBagInplace();

    abstract public Map<LgDeviceSlot, Integer> getCurrentQuantity();

    abstract public Map<LgDeviceSlot, Integer> getDesiredQuantity();

    abstract public boolean errorReset();

    abstract public boolean storingErrorReset();
//    public void onGloryEvent(ManagerStatus m) {
//        ActionState currState = state;
//        do {
//            Logger.debug("Action : OnGloryEvent state %s currState %s event %s",
//                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), m.toString());
//            currState = state;
//            currState.onGloryEvent(m);
//        } while (!state.equals(currState));
//    }

//    public void onIoBoardEvent(IoBoard.IoBoardStatus s) {
//        ActionState currState = state;
//        do {
//            Logger.debug("Action : onIoBoardEvent state %s currState %s event %s",
//                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), s.toString());
//            currState = state;
//            currState.onIoBoardEvent(s);
//        } while (!state.equals(currState));
//    }
//
//    public void onPrinterEvent(OSPrinter.PrinterStatus p) {
//        ActionState currState = state;
//        do {
//            Logger.debug("Action : onPrinterEvent state %s currState %s event %s",
//                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), p.toString());
//            currState = state;
//            state.onPrinterEvent(p);
//        } while (!state.equals(currState));
//    }
//
//    public void onTimeoutEvent(TimeoutTimer timer) {
//        Date currentDate = new Date();
//        TimeoutEvent.save(this, currentDate.toString());
//        state.onTimeoutEvent(timer);
//    }
    public boolean cancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void startEnvelopeDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    abstract public boolean count(Integer currency, Map<String, Integer> desiredQuantity);

}
