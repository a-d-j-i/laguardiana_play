package devices.serial;

import devices.device.DeviceMessageListenerInterface;
import devices.device.DeviceMessageInterface;
import java.security.InvalidParameterException;
import java.util.concurrent.atomic.AtomicBoolean;
import play.Logger;

/**
 * Device Adaptor
 * @author adji
 */
public class SerialPortReader implements Runnable, SerialPortAdapterInterface {

    private static void debug(String message, Object... args) {
        //Logger.debug(message, args);
    }

    final private Thread readerThread;
    final private AtomicBoolean mustStop = new AtomicBoolean(false);
    final SerialPortAdapterInterface serialPort;
    final SerialPortMessageParserInterface parser;
    final DeviceMessageListenerInterface listener;

    public SerialPortReader(SerialPortAdapterInterface serialPort, SerialPortMessageParserInterface parser, DeviceMessageListenerInterface listener) {
        this.serialPort = serialPort;
        this.parser = parser;
        this.listener = listener;
        readerThread = new Thread(this);
        if (serialPort == null) {
            Logger.error("Port is null");
            throw new InvalidParameterException("port is null");
        }
    }

    public void run() {
        debug("SerialPort Reader start");
        while (!mustStop.get()) {
            try {
                debug("Calling getMessage");
                DeviceMessageInterface msg = parser.getMessage(serialPort);
                if (msg != null) {
                    debug("Calling getMessage return %s", msg.toString());
                    listener.onDeviceMessageEvent(msg);
                }
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        debug("SerialPort Reader done");
    }

    public boolean open() {
        Logger.info("Opening %s", serialPort);
        boolean ret = serialPort.open();
        if (ret) {
            synchronized (readerThread) {
                readerThread.start();
            }
        }
        Logger.info("Opening %s %s", serialPort, ret ? "SUCCESS" : "FAIL");
        return ret;
    }

    public void close() {
        mustStop.set(true);
        readerThread.interrupt();
        try {
            synchronized (readerThread) {
                if (readerThread.isAlive()) {
                    readerThread.wait(30000);
                }
            }
        } catch (InterruptedException ex) {
            Logger.error("Error in serial port reader wait thread close.");
        }
        Logger.info("Closing %s", serialPort);
        if (serialPort != null) {
            serialPort.close();
        }
    }

    public boolean write(byte[] buffer) {
        return serialPort.write(buffer);
    }

    public Byte read() {
        return serialPort.read();
    }

    public Byte read(int timeoutMS) throws InterruptedException {
        return serialPort.read(timeoutMS);
    }

    public String readLine(int timeout) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
