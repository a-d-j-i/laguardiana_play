package devices.io_board;

import devices.SerialPortAdapterInterface;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import play.Logger;

/*
 * TODO: getCh, fifo, etc in other class.
 */
public class IoBoard {
    // read timeout in ms

    public static final int IOBOARD_READ_TIMEOUT = 10000;
    public static final int IOBOARD_STATUS_CHECK_FREQ = 2000;

    public enum Status {

        IDLE,
        OPENNING,
        OPEN,
        CLOSING,
        CLOSED,;
    }

    public class IoBoardStatus {

        public Integer A = 0;
        public Integer B = 0;
        public Integer C = 0;
        public Integer D = 0;
        public Status status = Status.IDLE;
        public boolean isRunning;
        public String error = null;
    }

    private class StatusThread extends Thread {

        private IoBoardStatus currentStatus = new IoBoardStatus();
        final private Lock mutex = new ReentrantLock();

        public IoBoardStatus getLastStatus(int timeout) throws InterruptedException {
            mutex.lock();
            try {
                return currentStatus;
            } finally {
                mutex.unlock();
            }
        }

        @Override
        public void run() {
            while (!mustStop.get()) {
                try {
                    if (serialPort == null) {
                        setError("StatusThread IoBoard Serial port closed");
                    } else {
                        String l = serialPort.readLine(IOBOARD_STATUS_CHECK_FREQ);
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
                    }
                } catch (Exception ex) {
                    if (ex.getCause() instanceof TimeoutException) {
                        Date currTime = new Date();
                        if (lastCmdSentTime != null && (currTime.getTime() - lastCmdSentTime.getTime()) > IOBOARD_READ_TIMEOUT) {
                            setError(String.format("StatusThread timeout reading from port, exception %s", ex.getMessage()));
                        } else {
                            sendCmd('S');
                        }
                    } else {
                        setError(String.format("StatusThread exception %s", ex.getMessage()));
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
            mutex.lock();
            try {
                currentStatus.status = s;
            } finally {
                mutex.unlock();
            }
        }

        private void setError(String error) {
            Logger.error(error);
            mutex.lock();
            try {
                currentStatus.error = error;
            } finally {
                mutex.unlock();
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
        sendCmd('O');
    }

    public void closeGate() {
        sendCmd('C');
    }

    private synchronized void sendCmd(char cmd) {
        if (serialPort == null) {
            Logger.error("IoBoard Serial port closed");
            return;
        }
        lastCmdSentTime = new Date();
        try {
            byte[] b = new byte[1];
            b[0] = (byte) cmd;
            if (cmd != 'S') {
                Logger.debug("IoBoard writting %c", cmd);
            }
            serialPort.write(b);
        } catch (IOException e) {
            Logger.error("IoBoard Error writing to port");
        }
    }

    public IoBoardStatus getStatus(int timeout) throws InterruptedException {
        return statusThread.getLastStatus(timeout);
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
