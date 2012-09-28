package devices;

import gnu.io.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import play.Logger;
import play.Play;

public final class SerialPortAdapterRxTx extends SerialPortAdapterAbstract implements SerialPortEventListener {

    public void serialEvent(SerialPortEvent spe) {
        if (serialPort == null) {
            Logger.error("Error reading serial port, port closed");
            return;
        }

        try {
            byte[] buffer = new byte[1024];
            int len;

            len = in.read(buffer);

            if (len > -1) {
                for (int i = 0; i < len; i++) {
                    fifoAdd(buffer[i]);
                }
            }
        } catch (IOException e) {
            Logger.error("Error reading serial port %s", e.getMessage());
        }
    }
    SerialPort serialPort;
    InputStream in;
    OutputStream out;

    // Hack the library path once!!!
    {
        String os = System.getProperty("os.name");
        if (os.indexOf("Win") == 0) {
            os = "Win";
        }

        File f = new File(Play.applicationPath.getAbsolutePath() + File.separator
                + "lib" + File.separator + os + File.separator
                + System.getProperty("os.arch"));

        Logger.debug("app path %s", f.getAbsolutePath());
        System.setProperty("java.library.path", f.getAbsolutePath());
        Field fieldSysPath;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (NoSuchFieldException ex) {
            Logger.error("Error setting library path %s", ex.getMessage());
        } catch (SecurityException ex) {
            Logger.error("Error setting library path %s", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.error("Error setting library path %s", ex.getMessage());
        } catch (IllegalAccessException ex) {
            Logger.error("Error setting library path %s", ex.getMessage());
        }
    }

    public SerialPortAdapterRxTx(String portN, PortConfiguration conf) throws IOException {
        super(conf);
        portName = portN;
        open();
    }

    @Override
    protected void open() throws IOException {
        CommPortIdentifier portIdentifier;

        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                throw new IOException("Error: Port is currently in use");
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (commPort instanceof CommPort) {
                    serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(conf.speed.getQ(), conf.bits.getQ(), conf.stop_bits.getQ(), conf.parity.getQ());
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    in = serialPort.getInputStream();
                    out = serialPort.getOutputStream();
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);
                } else {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            }
        } catch (Exception ex) {
            Logger.error("SerialPortAdapter : " + ex.getMessage());
            throw new IOException(String.format("Error initializing serial port %s", portName), ex);
        }
    }

    public void close() throws IOException {
        try {
            if (serialPort != null) {
                Logger.debug(String.format("Closing serial port %s", serialPort.getName()));
                in.close();
                out.close();
                serialPort.close();
                serialPort = null;
            }
        } catch (IOException e) {
            throw new IOException(String.format("Error closing serial port"), e);
        }
    }

    public void write(byte[] buffer) throws IOException {
        if (serialPort == null) {
            throw new IOException("Error wrting to serial port, port closed");
        }

        try {
            out.write(buffer);
        } catch (IOException e) {
            throw new IOException(String.format("Error wrting to serial port %s", serialPort.getName()), e);
        }
    }
}
