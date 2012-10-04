package devices;

import devices.IoBoard.IoBoardStatus;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
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

    public static final int IOBOARD_READ_TIMEOUT = 10000;
    public static final int IOBOARD_STATUS_CHECK_FREQ = 2000;
    public static final int IOBOARD_MAX_RETRIES = 5;

    public enum Status {

        IDLE,
        ERROR,
        OPENNING,
        OPEN,
        CLOSING,
        CLOSED,
        //
        READY_TO_STORE,;
    }

    public class IoBoardStatus implements Cloneable {

        public Integer A = 0;
        public Integer B = 0;
        public Integer C = 0;
        public Integer D = 0;
        public Status status = Status.IDLE;
        public boolean isRunning;
        public String error = null;

        private IoBoardStatus copy() {
            try {
                return (IoBoardStatus) this.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.debug("in copy CloneNotSupportedException");
                return null;
            }
        }

        @Override
        public String toString() {
            return "IoBoardStatus{" + "A=" + A + ", B=" + B + ", C=" + C + ", D=" + D + ", status=" + status + ", isRunning=" + isRunning + ", error=" + error + '}';
        }
    }

    private class StatusThread extends Thread {

        private IoBoardStatus currentStatus = new IoBoardStatus();
        final private Lock mutex = new ReentrantLock();

        public IoBoardStatus getStatus() {
            mutex.lock();
            try {
                return currentStatus.copy();
            } finally {
                mutex.unlock();
            }
        }

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
                            setABCD(A, B, C, D);
                        } catch (NumberFormatException e) {
                            Logger.warn("checkStatus invalid number: %s", e.getMessage());
                        }
                    } else {
                        if (l.equalsIgnoreCase("START OPEN")) {
                            setStatus(Status.OPENNING);
                        } else if (l.equalsIgnoreCase("START CLOSE")) {
                            setStatus(Status.CLOSING);
                        } else if (l.equalsIgnoreCase("OPEN")) {
                            setStatus(Status.OPEN);
                        } else if (l.equalsIgnoreCase("CLOSE")) {
                            setStatus(Status.CLOSED);
                        } else if (l.equalsIgnoreCase("ERROR OPEN TIMEOUT")) {
                            setError("Open timeout");
                        } else {
                            Logger.warn("StatusThread Ignoring line : %s", l);
                        }
                    }
                } catch (Exception ex) {
                    if (!(ex.getCause() instanceof TimeoutException)) {
                        setError(String.format("StatusThread exception %s", ex.getMessage()));
                    } else { // timeout
                        // Try again
                        if (getStatus().status != Status.ERROR) {
                            sendCmd('S');
                            retries++;

                            if (retries == IOBOARD_MAX_RETRIES) {
                                setError(String.format("StatusThread timeout reading from port, exception %s", ex.getMessage()));
                                retries = 0;
                            }
                            Date currTime = new Date();
                            if (lastCmdSentTime != null && (currTime.getTime() - lastCmdSentTime.getTime()) > IOBOARD_READ_TIMEOUT) {
                                setError(String.format("StatusThread timeout reading from port, exception %s", ex.getMessage()));
                                try {
                                    serialPort.reconect();
                                } catch (IOException ex1) {
                                    setError(String.format("Error in reconection %s", ex1.getMessage()));
                                }
                            }
                        }
                    }
                }
            }
            Logger.debug("IOBOARD status thread done");
        }

        private void setABCD(Integer A, Integer B, Integer C, Integer D) {
            mutex.lock();
            try {
                currentStatus.A = A;
                currentStatus.B = B;
                currentStatus.C = C;
                currentStatus.D = D;
            } finally {
                mutex.unlock();
            }
        }

        private void setStatus(Status s) {
            IoBoardStatus status;
            mutex.lock();
            try {
                currentStatus.status = s;
                status = currentStatus.copy();
            } finally {
                mutex.unlock();
            }
            setChanged();
            notifyObservers(status);
        }

        private void setError(String error) {
            Logger.error("------- > IoBoard error : %s", error);
            mutex.lock();
            try {
                currentStatus.status = Status.ERROR;
                currentStatus.error = error;
            } finally {
                mutex.unlock();
            }
        }

        private void clearError() {
            mutex.lock();
            try {
                // Don't overwrite the first error!!!.
                if (currentStatus.status == Status.ERROR) {
                    currentStatus.status = Status.IDLE;
                }
            } finally {
                mutex.unlock();
            }
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
                    if (getStatus().status != Status.ERROR) {
                        setError(String.format("Error in reconection %s", ex1.getMessage()));
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

    public IoBoardStatus getStatus() {
        return statusThread.getStatus();
    }

    public void clearError() {
        statusThread.clearError();
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