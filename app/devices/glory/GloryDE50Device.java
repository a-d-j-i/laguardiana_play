package devices.glory;

import devices.DeviceAbstract;
import devices.DeviceClassCounterIntreface;
import devices.DeviceStatus;
import devices.glory.operation.GloryOperationAbstract;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.OpenPort;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import machines.Machine;
import models.Configuration;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract implements DeviceClassCounterIntreface {

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

        final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
        final private static int GLORY_READ_TIMEOUT = 5000;
        final private GloryDE50 gl = new GloryDE50(GLORY_READ_TIMEOUT);

        boolean closing = false;

        public GloryDE50OperationResponse sendGloryDE50Operation(GloryOperationAbstract operation) {
            return gl.sendOperation(operation);
        }

        public GloryDE50OperationResponse sendGloryDE50Operation(GloryOperationAbstract operation, boolean debug) {
            return gl.sendOperation(operation, debug);
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

        private boolean portOpen = false;

        public boolean open(String value) {
            Logger.debug("api open");
            portOpen = false;
            SerialPortAdapterInterface serialPort = Configuration.getSerialPort(value, portConf);
            Logger.info(String.format("Configuring serial port %s", serialPort));
            gl.close();
            Logger.debug("Glory port open try");
            boolean ret = gl.open(serialPort);
            Logger.debug("Glory port open : %s", ret ? "success" : "fails");
            if (ret) {
                portOpen = true;
            }
            return ret;
        }

        public boolean isPortOpen() {
            return portOpen;
        }

    }
    final private GloryDE50StateMachineApi api = new GloryDE50StateMachineApi();
    final private AtomicReference<GloryDE50StateAbstract> currentState = new AtomicReference<GloryDE50StateAbstract>(new OpenPort(api));

    public GloryDE50Device(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc);
    }

    @Override
    protected void initDeviceProperties() {
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        currentState.get().openPort(lgSerialPort.value, false);
    }

    @Override
    public void assemble() {
    }

    @Override
    public void mainLoop() {
        Logger.debug(String.format("Glory executing current step: %s", currentState.get().getClass().getSimpleName()));
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
        api.gl.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    @Override
    protected boolean changeProperty(String property, final String value) {
        // TODO: enum
        if (property.compareToIgnoreCase("port") == 0) {
            return currentState.get().openPort(value, true);
        }
        return false;
    }

    // Pass it to the device thread and wait for a result only if the device thread is ready for it.
    // Neve use it as a functional stuff, just for testing.
    public GloryDE50OperationResponse sendGloryDE50Operation(final GloryOperationAbstract operation) {
        return sendGloryDE50Operation(operation, false);
    }

    public GloryDE50OperationResponse sendGloryDE50Operation(final GloryOperationAbstract operation, final boolean debug) {
        final FutureTask<GloryDE50OperationResponse> t = new FutureTask<GloryDE50OperationResponse>(new Callable<GloryDE50OperationResponse>() {
            public GloryDE50OperationResponse call() throws Exception {
                return api.sendGloryDE50Operation(operation, debug);
            }
        });
        if (currentState.get().sendOperation(t)) {
            try {
                return t.get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }
        return null;
    }

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

    public boolean clearError() {
        return currentState.get().clearError();
    }

    @Override
    public DeviceStatus getStatus() {
        GloryDE50StateAbstract st = currentState.get();
        return new DeviceStatus(st.getError());
    }
}
