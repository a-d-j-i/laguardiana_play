package devices;

import gnu.io.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import play.Logger;
import play.Play;

public final class SerialPortAdapterRxTx extends SerialPortAdapterAbstract implements SerialPortEventListener {

    public void serialEvent(SerialPortEvent spe) {
        if (serialPort == null || in == null) {
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
    SerialPort serialPort = null;
    InputStream in = null;
    OutputStream out = null;

    // Hack the library path once!!!
    static {
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

    public SerialPortAdapterRxTx(String portN, PortConfiguration conf) {
        super(conf);
        portName = portN;
        try {
            open();
        } catch (IOException ex) {
            Logger.error("Error opening the serial port, must reopen later");
        }
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
                    Logger.error("Error: Only serial ports are handled by this example.");
                }
            }
        } catch (Exception ex) {
            Logger.error("SerialPortAdapter Exception: " + ex.toString() + " " + ex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            Logger.error("SerialPortAdapter Exception: " + sw.toString());
            throw new IOException(String.format("Error initializing serial port %s", portName), ex);
        }
    }

    public void close() throws IOException {
        if (serialPort != null) {
            Logger.debug(String.format("Closing serial port %s", serialPort.getName()));
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                throw new IOException(String.format("Error closing serial port in"), e);
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                throw new IOException(String.format("Error closing serial port out"), e);
            }
            try {
                if (serialPort != null) {
                    serialPort.close();
                }
                serialPort = null;
            } catch (Exception e) {
                throw new IOException(String.format("Error closing serial port"), e);
            }
        }
    }

    public void write(byte[] buffer) throws IOException {
        if (serialPort == null) {
            throw new IOException("Error writing to serial port, port closed");
        }

        try {
            if (out != null) {
                out.write(buffer);
            } else {
                throw new IOException(String.format("Error writing to serial port %s, out is null", serialPort.getName()));
            }
        } catch (IOException e) {
            throw new IOException(String.format("Error writing to serial port %s", serialPort.getName()), e);
        }
    }
}
