package devices;

import devices.IoBoard.IoBoardStatus;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import play.Logger;

/*
 * TODO: getCh, fifo, etc in other class.
 */
public class IoBoard extends Observable {

    // read timeout in ms
    public enum SHUTTER_STATE {

        SHUTTER_START(0),
        SHUTTER_START_OPEN(1),
        SHUTTER_START_CLOSE(2),
        SHUTTER_RUN_OPEN(3),
        SHUTTER_RUN_CLOSE(4),
        SHUTTER_RUN_OPEN_PORT(5),
        SHUTTER_RUN_CLOSE_PORT(6),
        SHUTTER_STOP_OPEN(7),
        SHUTTER_STOP_CLOSE(8),
        SHUTTER_OPEN(9),
        SHUTTER_CLOSED(10);
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

    public enum BAG_STATE {

        BAG_STATE_ERROR(0),
        BAG_STATE_REMOVED(1),
        BAG_STATE_TAKING_S(2),
        BAG_STATE_TAKING_SM(3),
        BAG_STATE_PLACING_S(4),
        BAG_STATE_PLACING_SM(5),
        BAG_STATE_INPLACE(6);
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
    public static final int IOBOARD_READ_TIMEOUT = 10000;
    public static final int IOBOARD_STATUS_CHECK_FREQ = 2000;
    public static final int IOBOARD_MAX_RETRIES = 5;

    public class IoBoardStatus implements Cloneable {

        final private Lock mutex = new ReentrantLock();
        public Integer A = 0;
        public Integer B = 0;
        public Integer C = 0;
        public Integer D = 0;
        public Integer bagStatus = 0;
        public boolean isRunning;
        public String error = null;
        public BAG_STATE bagState = null;
        public SHUTTER_STATE shutterState = null;
        public Integer lockState = null;

        private IoBoardStatus copy() {
            try {
                return (IoBoardStatus) this.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.debug("in copy CloneNotSupportedException");
                return null;
            }
        }

        private void setBagState(BAG_STATE bagState) {
            if (this.bagState != bagState) {
                this.bagState = bagState;
                setChanged();
            }
        }

        private void setShutterState(SHUTTER_STATE shutterState) {
            if (this.shutterState != shutterState) {
                this.shutterState = shutterState;
                setChanged();
            }
        }

        private void setLockState(Integer lockState) {
            if (this.lockState != lockState) {
                this.lockState = lockState;
                setChanged();
            }
        }

        public IoBoardStatus getStatusCopy() {
            mutex.lock();
            try {
                return copy();
            } finally {
                mutex.unlock();
            }
        }

        private void setSTATE(Integer bagSt, Integer shutterSt, Integer lockSt) {
            IoBoardStatus status = null;
            mutex.lock();
            try {
                setBagState(BAG_STATE.factory(bagSt));
                setShutterState(SHUTTER_STATE.factory(shutterSt));
                setLockState(lockSt);
                this.lockState = lockSt;
                if (hasChanged()) {
                    status = copy();
                }
            } finally {
                mutex.unlock();
            }
            notifyObservers(status);
        }

        private void setStatus(Integer A, Integer B, Integer C, Integer D, Integer BAG) {
            mutex.lock();
            try {
                this.A = A;
                this.B = B;
                this.C = C;
                this.D = D;
                this.bagStatus = BAG;
            } finally {
                mutex.unlock();
            }
        }

        private void setError(String error) {
            IoBoardStatus status = null;
            Logger.error("------- > IoBoard error : %s", error);
            mutex.lock();
            try {
                this.error = error;
                setChanged();
                status = copy();
            } finally {
                mutex.unlock();
            }
            notifyObservers(status);
        }

        private void clearError() {
            mutex.lock();
            try {
                // Don't overwrite the first error!!!.
                this.error = null;
            } finally {
                mutex.unlock();
            }
        }

        private String getError() {
            String ret;
            mutex.lock();
            try {
                // Don't overwrite the first error!!!.
                ret = this.error;
            } finally {
                mutex.unlock();
            }
            return ret;
        }

        public boolean isError() {
            return getError() != null;
        }

        @Override
        public String toString() {
            return "IoBoardStatus{" + "A=" + A + ", B=" + B + ", C=" + C + ", D=" + D + ", bagStatus=" + bagStatus + ", isRunning=" + isRunning + ", error=" + error + ", bagState=" + bagState + ", shutterState=" + shutterState + ", lockState=" + lockState + '}';
        }
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
                    Logger.debug("IOBOARD reader %s", l);
                    retries = 0;
                    if (l.startsWith("STATUS :") && l.length() == 31) {
                        try {
                            Integer A = Integer.parseInt(l.substring(11, 13), 16);
                            Integer B = Integer.parseInt(l.substring(17, 19), 16);
                            Integer C = Integer.parseInt(l.substring(23, 25), 16);
                            Integer D = Integer.parseInt(l.substring(29, 31), 16);
                            Integer BAG = Integer.parseInt(l.substring(68, 70), 16);
                            currentStatus.setStatus(A, B, C, D, BAG);
                        } catch (NumberFormatException e) {
                            Logger.warn("checkStatus invalid number: %s", e.getMessage());
                        }
                    } else if (l.startsWith("STATE :") && l.length() == 31) {
                        try {
                            Integer bagSt = Integer.parseInt(l.substring(12, 14), 10);
                            Integer shutterSt = Integer.parseInt(l.substring(23, 25), 10);
                            Integer lockSt = Integer.parseInt(l.substring(31, 32), 10);
                            // TODO: Lockin
                            currentStatus.setSTATE(bagSt, shutterSt, lockSt);
                        } catch (NumberFormatException e) {
                            Logger.warn("checkStatus invalid number: %s", e.getMessage());
                        }
                    } else if (l.startsWith("ERROR")) {
                        currentStatus.setError(l);
                    } else {
                        Logger.warn("IOBOARD Ignoring line : %s", l);
                    }
                } catch (Exception ex) {
                    if (!(ex.getCause() instanceof TimeoutException)) {
                        currentStatus.setError(String.format("StatusThread exception %s", ex.getMessage()));
                    } else { // timeout
                        // Try again
                        if (getError() == null) {
                            sendCmd('S');
                            retries++;

                            if (retries == IOBOARD_MAX_RETRIES) {
                                currentStatus.setError(String.format("StatusThread timeout reading from port, exception %s", ex.getMessage()));
                                retries = 0;
                            }
                            Date currTime = new Date();
                            if (lastCmdSentTime != null && (currTime.getTime() - lastCmdSentTime.getTime()) > IOBOARD_READ_TIMEOUT) {
                                currentStatus.setError(String.format("StatusThread timeout reading from port, exception %s", ex.getMessage()));
                                try {
                                    serialPort.reconect();
                                } catch (IOException ex1) {
                                    currentStatus.setError(String.format("Error in reconection %s", ex1.getMessage()));
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
                Logger.debug("IoBoard writting %c", cmd);
                serialPort.write(b);
                lastCmdSentTime = new Date();
            } catch (IOException e) {
                Logger.error("IoBoard Error writing to port %s", e.getMessage());
                try {
                    serialPort.reconect();
                } catch (IOException ex1) {
                    if (getError() == null) {
                        currentStatus.setError(String.format("Error in reconection %s", ex1.getMessage()));
                    }
                }
            }
        }
    }
    private IoBoardStatus currentStatus = new IoBoardStatus();
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

    public void clearError() {
        statusThread.sendCmd('E');
        currentStatus.clearError();
    }

    public boolean isError() {
        return currentStatus.isError();
    }

    public String getError() {
        return currentStatus.getError();
    }

    public IoBoardStatus getStatusCopy() {
        return currentStatus.getStatusCopy();
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
