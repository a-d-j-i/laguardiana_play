package devices.glory;

import devices.DeviceAbstract;
import devices.DeviceAbstract.DeviceType;
import devices.DeviceClassCounterIntreface;
import devices.DeviceStatus;
import devices.glory.command.GloryOperationAbstract;
import devices.glory.response.GloryDE50Response;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.OpenPort;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract implements DeviceClassCounterIntreface {


    /* FROM GLORY !!! */
    static public enum STATUS {

        NEUTRAL,
        READY_TO_STORE,
        STORING,
        STORED,
        PUT_THE_BILLS_ON_THE_HOPER,
        COUNTING,
        ESCROW_FULL,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,
        CANCELING, BAG_COLLECTED,
        JAM,
        ERROR;
    };

    public class GloryDE50StateMachineApi {

        public boolean open() {
            return gl.open();
        }

        public GloryDE50Response sendGloryOperation(GloryOperationAbstract cmd) {
            return gl.sendCommand(cmd);
        }

        public void notifyListeners(String details) {
        }

        public void notifyListeners(STATUS status) {
        }

        public boolean isClosing() {
            return closing;
        }

        public void setClosing(boolean b) {
            closing = b;
        }

    }
    boolean closing = false;
    final public static int GLORY_READ_TIMEOUT = 5000;
    private final AtomicReference<GloryDE50StateAbstract> currentState = new AtomicReference<GloryDE50StateAbstract>();
    private GloryDE50 gl = null;
    LgDeviceProperty lgSerialPort;

//    public GloryManager(GloryDE50 device) {
    public GloryDE50Device(DeviceType deviceType, String machineDeviceId) {
        super(deviceType, machineDeviceId);
    }

    @Override
    public DeviceStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initDeviceProperties() {
        lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
    }

    @Override
    public void assemble() {
        currentState.set(new OpenPort(new GloryDE50StateMachineApi()));
    }

    @Override
    public void mainLoop() {
        Logger.debug(String.format("Glory executing current step: %s", currentState.getClass().getSimpleName()));
        GloryDE50StateAbstract oldState = currentState.get();
        GloryDE50StateAbstract newState = oldState.step();
        if (newState != null && oldState != newState) {
            GloryDE50StateAbstract initState = newState.init();
            if (initState != null) {
                newState = initState;
            }
            currentState.set(newState);
        }
    }

    @Override
    public void disassemble() {
        Logger.debug("Executing GotoNeutral command on Stop");
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    @Override
    protected void changeProperty(LgDeviceProperty lgdp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // The trick to avoid race conditions here is that count only change a variable in the state or fails
    // the outside thread is not able to change the state.
    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        return currentState.get().count(desiredQuantity, currency);
    }

    public boolean envelopeDeposit() {
        return currentState.get().envelopeDeposit();
    }

    public boolean collect() {
        return currentState.get().collect();
    }

    public boolean reset() {
        return currentState.get().reset();
    }

    public boolean storingErrorReset() {
        return currentState.get().storingErrorReset();
        // syncronous cancel and storingErrorReset.
    }

    public boolean cancelDeposit() {
        return currentState.get().cancelDeposit();
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        return currentState.get().storeDeposit(sequenceNumber);
    }

    public boolean withdrawDeposit() {
        return currentState.get().withdrawDeposit();
    }

    public Integer getCurrency() {
        return currentState.get().getCurrency();
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return currentState.get().getCurrentQuantity();
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return currentState.get().getDesiredQuantity();
    }
}
