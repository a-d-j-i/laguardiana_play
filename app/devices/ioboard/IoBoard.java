package devices.ioboard;

import devices.SerialPortAdapterInterface;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import models.Configuration;
import play.Logger;

/*
 * TODO: getCh, fifo, etc in other class.
 */
public class IoBoard {

    // read timeout in ms
    public enum SHUTTER_STATE {

        SHUTTER_START_CLOSE(0),
        SHUTTER_START_OPEN(1),
        SHUTTER_RUN_OPEN(2),
        SHUTTER_RUN_CLOSE(3),
        SHUTTER_RUN_OPEN_PORT(4),
        SHUTTER_RUN_CLOSE_PORT(5),
        SHUTTER_STOP_OPEN(6),
        SHUTTER_STOP_CLOSE(7),
        SHUTTER_OPEN(8),
        SHUTTER_CLOSED(9);
        static final HashMap< Byte, SHUTTER_STATE> reverse = new HashMap< Byte, SHUTTER_STATE>();

        static {
            for (SHUTTER_STATE s : SHUTTER_STATE.values()) {
                reverse.put(s.stId, s);
            }
        }
        private Byte stId;

        private SHUTTER_STATE(int stId) {
            this.stId = (byte) stId;
        }

        public static SHUTTER_STATE factory(int stId) {
            return reverse.get((byte) stId);
        }

        public boolean isOpen() {
            return this == SHUTTER_OPEN;
        }
    };

    public enum BAG_APROVE_STATE {

        BAG_APROVED,
        BAG_NOT_APROVED,
        BAG_APROVE_WAIT,
        BAG_APROVE_CONFIRM,;
    };

    public enum BAG_STATE {

        BAG_STATE_ERROR(0),
        BAG_STATE_REMOVED(1),
        BAG_STATE_INPLACE(2),
        BAG_STATE_TAKING_START(3),
        BAG_STATE_TAKING_STEP1(4),
        BAG_STATE_TAKING_STEP2(5),
        BAG_STATE_PUTTING_START(6),
        BAG_STATE_PUTTING_1(7),
        BAG_STATE_PUTTING_2(8),
        BAG_STATE_PUTTING_3(9),
        BAG_STATE_PUTTING_4(10),
        BAG_STATE_PUTTING_5(11),
        BAG_STATE_PUTTING_6(12),;
        static final HashMap< Byte, BAG_STATE> reverse = new HashMap< Byte, BAG_STATE>();

        static {
            for (BAG_STATE s : BAG_STATE.values()) {
                reverse.put(s.stId, s);
            }
        }
        private Byte stId;

        private BAG_STATE(int stId) {
            this.stId = (byte) stId;
        }

        public static BAG_STATE factory(int stId) {
            return reverse.get((byte) stId);
        }
    }

    // An immutable cloned version of IoBoardState 
    public class IoBoardStatus {

        private final SHUTTER_STATE shutterState;
        private final BAG_APROVE_STATE bagAproveState;
        private final BAG_STATE bagState;
        private final IoBoardError error;
        private final String criticalEvent;

        private IoBoardStatus(State currentState) {
            this.shutterState = currentState.shutterState;
            this.bagAproveState = currentState.bagAproveState;
            this.error = currentState.error;
            this.bagState = currentState.bagState;
            this.criticalEvent = null;
        }

        private IoBoardStatus(State currentState, String criticalEvent) {
            this.shutterState = currentState.shutterState;
            this.bagAproveState = currentState.bagAproveState;
            this.error = currentState.error;
            this.bagState = currentState.bagState;
            this.criticalEvent = criticalEvent;
        }

        public SHUTTER_STATE getShutterState() {
            return shutterState;
        }

        public BAG_APROVE_STATE getBagAproveState() {
            return bagAproveState;
        }

        public BAG_STATE getBagState() {
            return bagState;
        }

        public IoBoardError getError() {
            return error;
        }

        public String getCriticalEvent() {
            return criticalEvent;
        }

        @Override
        public String toString() {
            return "IoBoardStatus{" + "shutterState=" + shutterState + ", bagAproveState=" + bagAproveState + ", bagState=" + bagState + ", error=" + error + '}';
        }
    }
    // A singleton create to hold the state of the ioboard.

    private class State extends Observable {

        private Byte A = 0;
        private Byte B = 0;
        private Byte C = 0;
        private Byte D = 0;
        private Byte BAG_STATUS = 0;
        private Byte BAG_SENSOR = 0;
//        private boolean isRunning;
        private IoBoardError error = null;
        private BAG_STATE bagState = null;
        private SHUTTER_STATE shutterState = null;
        private Integer lockState = null;
        private BAG_APROVE_STATE bagAproveState = BAG_APROVE_STATE.BAG_APROVED;

        synchronized private void setSTATE(Integer bagSt, Integer shutterSt, Integer lockSt, Boolean bagAproved) {
            //Logger.debug("IOBOARD setSTATE : bagSt %s, setShutterState : %s, setLockState : %d, bagAproved : %s", bagSt, shutterSt, lockSt, bagAproved);
            BAG_STATE bs = BAG_STATE.factory(bagSt);
            if (bs != bagState) {
                bagState = bs;
                setChanged();
            }
            SHUTTER_STATE ss = SHUTTER_STATE.factory(shutterSt);
            if (ss != shutterState) {
                shutterState = ss;
                setChanged();
            }
            if (lockSt != lockState) {
                lockState = lockSt;
                setChanged();
            }
            // The bag changed the state.
            switch (bagAproveState) {
                case BAG_APROVED:
                    if (!bagAproved) {
                        Logger.debug("IOBOARD BAG NOT APROVED");
                        bagAproveState = BAG_APROVE_STATE.BAG_NOT_APROVED;
                        setChanged();
                    }
                    break;
                case BAG_APROVE_WAIT:
                    if (bagAproved) {
                        Logger.debug("IOBOARD BAG APROVE CONFIRM");
                        bagAproveState = BAG_APROVE_STATE.BAG_APROVE_CONFIRM;
                        setChanged();
                    }
                    break;
                case BAG_APROVE_CONFIRM:
                case BAG_NOT_APROVED:
                    break;
            }
            if (hasChanged()) {
                Logger.debug("IOBOARD setSTATE prev: bagSt %s, setShutterState : %s, setLockState : %d, bagAproved : %s", bagSt, shutterSt, lockSt, bagAproved);
                Logger.debug("IOBOARD setSTATE next: bagState %s, shutterState : %s, lockState : %d, bagAproveState : %s", bagState, shutterState, lockState, bagAproveState);
                notifyObservers(new IoBoardStatus(this));
            }
            //Logger.debug("IOBOARD setSTATE : bagState %s, shutterState : %s, lockState : %d, bagAproveState : %s", bagState, shutterState, lockState, bagAproveState);
        }

        synchronized private void setStatusBytes(Byte A, Byte B, Byte C, Byte D, Byte BAG_SENSOR, Byte BAG_STATUS) {
            //Logger.debug("IOBOARD setStatusBytes : A 0x%x B 0x%x C 0x%x D 0x%x BAG_SENSOR 0x%x BAG_STATUS 0x%x", A, B, C, D, BAG_SENSOR, BAG_STATUS);
            this.A = A;
            this.B = B;
            this.C = C;
            this.D = D;
            this.BAG_SENSOR = BAG_SENSOR;
            this.BAG_STATUS = BAG_STATUS;
        }

        synchronized private void setError(IoBoardError error) {
            this.error = error;
            setChanged();
            notifyObservers(new IoBoardStatus(this));
        }
        // Critical states

        synchronized private void setCriticalEvent(String criticalEvent) {
            setChanged();
            notifyObservers(new IoBoardStatus(this, criticalEvent));

        }

        synchronized private void setAproveBagState(BAG_APROVE_STATE state) {
            this.bagAproveState = state;
            setChanged();
            notifyObservers(new IoBoardStatus(this));
        }

        synchronized public void clearError() {
            // Don't overwrite the first error!!!.
            this.error = null;
            statusThread.sendCmd('E');
            setChanged();
            notifyObservers(new IoBoardStatus(this));
        }

        synchronized private IoBoardError getError() {
            return error;
        }

        public String repr(Byte d) {
            BitSet b = new BitSet();
            byte c = 1;
            for (int i = 0; i < 8; i++) {
                if ((d & c) != 0) {
                    b.set(i);
                }
                c <<= 1;
            }
            return String.format("0x%02X : %s", d, b.toString());
        }

        public String reprA() {
            return repr(A);
        }

        public String reprB() {
            return repr(B);
        }

        public String reprC() {
            return repr(C);
        }

        public String reprD() {
            return repr(D);
        }

        public String reprBAG_SENSOR() {
            return repr(BAG_SENSOR);
        }

        public String reprBAG_STATUS() {
            return repr(BAG_STATUS);
        }
    }
    public static final int IOBOARD_READ_TIMEOUT = 10000;
    public static final int IOBOARD_STATUS_CHECK_FREQ = 500;
    public static final int IOBOARD_MAX_RETRIES = 5;
    final private State state = new State();

    @Override
    public String toString() {
        return "IoBoard{" + "state=" + state + ", lastCmdSentTime=" + lastCmdSentTime + ", mustStop=" + mustStop + ", statusThread=" + statusThread + ", serialPort=" + serialPort + '}';
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 3];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    private class StatusThread extends Thread {

        @Override
        public void run() {
            int retries = 0;
            Logger.debug("IoBoard status thread started");
            while (!mustStop.get()) {
                try {
                    if (serialPort == null) {
                        throw new IOException("IoBoard StatusThread IoBoard Serial port closed");
                    }
                    String l = serialPort.readLine(IOBOARD_STATUS_CHECK_FREQ);
                    //Logger.debug("IOBOARD %d reader : %s", l.length(), l);
                    //Logger.debug("IOBOARD reader %s", bytesToHex(l.getBytes()));
                    retries = 0;
                    if (l.startsWith("STATUS :") && l.length() > 71) {
                        try {
                            Integer A = Integer.parseInt(l.substring(13, 15), 16);
                            Integer B = Integer.parseInt(l.substring(21, 23), 16);
                            Integer C = Integer.parseInt(l.substring(29, 31), 16);
                            Integer D = Integer.parseInt(l.substring(37, 39), 16);
                            Integer BAG_SENSOR = Integer.parseInt(l.substring(54, 56), 16);
                            Integer BAG_STATUS = Integer.parseInt(l.substring(70, 72), 16);
                            //Logger.debug("------------- 0x%02X 0x%02X 0x%02X 0x%02X ", A, B, C, D);
                            state.setStatusBytes(A.byteValue(), B.byteValue(), C.byteValue(), D.byteValue(), BAG_STATUS.byteValue(), BAG_SENSOR.byteValue());
                        } catch (NumberFormatException e) {
                            Logger.warn("checkStatus invalid number: %s", e.getMessage());
                        }
                    } else if (l.startsWith("STATE :") && l.length() > 45) {
                        try {
                            Integer bagSt = Integer.parseInt(l.substring(12, 14), 10);
                            Boolean bagAproved = (Integer.parseInt(l.substring(27, 28), 10) == 1);
                            Integer shutterSt = Integer.parseInt(l.substring(37, 39), 10);
                            Integer lockSt = Integer.parseInt(l.substring(45, 46), 10);
                            // TODO: Lockin
                            state.setSTATE(bagSt, shutterSt, lockSt, bagAproved);
                        } catch (NumberFormatException e) {
                            Logger.warn("checkStatus invalid number: %s", e.getMessage());
                        }
                    } else if (l.startsWith("CRITICAL :") && l.length() > 45) {
                        state.setCriticalEvent(l);
                    } else if (l.contains("ERROR")) {
                        if (l.contains("SHUTTER") && Configuration.isIgnoreShutter()) {
                            // Ignore.
                        } else {
                            state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_FW_ERROR, l));
                        }
                    } else {
                        Logger.warn("IOBOARD Ignoring line : %s", l);
                    }
                } catch (Exception ex) {
                    if (!(ex.getCause() instanceof TimeoutException)) {
                        state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_COMMUNICATION_TIMEOUT,
                                String.format("StatusThread exception %s %s %s", ex, ex.getCause(), ex.getMessage())));
                        //throw new RuntimeException( ex );
                    } else { // timeout
                        // Try again
                        if (state.getError() == null) {
                            sendCmd('S');
                            retries++;

                            if (retries == IOBOARD_MAX_RETRIES) {
                                state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_COMMUNICATION_TIMEOUT,
                                        String.format("StatusThread timeout reading from port, exception %s", ex.getMessage())));
                                retries = 0;
                            }
                            Date currTime = new Date();
                            if (lastCmdSentTime != null && (currTime.getTime() - lastCmdSentTime.getTime()) > IOBOARD_READ_TIMEOUT) {
                                state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_COMMUNICATION_TIMEOUT,
                                        String.format("StatusThread timeout reading from port, exception %s", ex.getMessage())));
                                try {
                                    serialPort.reconect();
                                } catch (IOException ex1) {
                                    state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_COMMUNICATION_TIMEOUT,
                                            String.format("Error in reconection %s", ex1.getMessage())));
                                }
                            }
                        }
                    }
                }
            }
            Logger.debug("IOBOARD status thread done");
        }

        private void sendCmd(char cmd) {
            if (serialPort == null) {
                Logger.error("IoBoard Serial port closed");
                return;
            }
            try {
                byte[] b = new byte[1];
                b[0] = (byte) cmd;
                if (cmd != 'S') {
                    Logger.debug("IoBoard writting %c", cmd);
                }
                serialPort.write(b);
                lastCmdSentTime = new Date();
            } catch (IOException e) {
                Logger.error("IoBoard Error writing to port %s", e.getMessage());
                try {
                    serialPort.reconect();
                } catch (IOException ex1) {
                    if (state.getError() == null) {
                        state.setError(new IoBoardError(IoBoardError.ERROR_CODE.IOBOARD_COMMUNICATION_ERROR,
                                String.format("Error in reconection %s", ex1.getMessage())));
                    }
                }
            }
        }
    }
    private Date lastCmdSentTime = null;
    private AtomicBoolean mustStop = new AtomicBoolean(false);
    private final StatusThread statusThread;
    private SerialPortAdapterInterface serialPort = null;

    public IoBoard(SerialPortAdapterInterface serialPort) {
        if (serialPort == null) {
            throw new InvalidParameterException("IoBoard invalid parameter serial port");
        }
        if (this.serialPort != null) {
            throw new InvalidParameterException("IoBoard serial port allready open");
        }
        this.serialPort = serialPort;
        statusThread = new StatusThread();
    }

    public IoBoardStatus getStatus() {
        return new IoBoardStatus(state);
    }

    public State getInternalState() {
        return state;
    }

    public void startStatusThread() {
        Logger.debug("IoBoard status thread start");
        statusThread.start();
    }

    public void close() {
        mustStop.set(true);
        try {
            statusThread.join(IOBOARD_READ_TIMEOUT * 2);
        } catch (InterruptedException ex) {
            Logger.error("Error closing the ioboard status thread %s", ex.getMessage());
        }
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException ex) {
                Logger.error("Error closing the ioboard serial port %s", ex.getMessage());
            }
        }
        serialPort = null;
    }

    public void openGate() {
        statusThread.sendCmd('O');
    }

    public void closeGate() {
        statusThread.sendCmd('C');
    }

    public void aproveBag() {
        state.setAproveBagState(BAG_APROVE_STATE.BAG_APROVE_WAIT);
        statusThread.sendCmd('A');
    }

    public void aproveBagConfirm() {
        state.setAproveBagState(BAG_APROVE_STATE.BAG_APROVED);
    }

    public void clearError() {
        state.clearError();
    }

    public IoBoardError getError() {
        return state.getError();
    }

    public void addObserver(Observer observer) {
        state.addObserver(observer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IoBoard other = (IoBoard) obj;
        if (this.serialPort != other.serialPort && (this.serialPort == null || !this.serialPort.equals(other.serialPort))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.serialPort != null ? this.serialPort.hashCode() : 0);
        return hash;
    }
}
