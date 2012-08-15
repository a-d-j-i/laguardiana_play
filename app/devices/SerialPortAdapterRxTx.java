package devices;

import gnu.io.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import play.Logger;
import play.Play;

public class SerialPortAdapterRxTx extends SerialPortAdapterAbstract {

    SerialPort serialPort;
    InputStream in;
    OutputStream out;

    public SerialPortAdapterRxTx(String portN) throws IOException {
        portName = portN;
        CommPortIdentifier portIdentifier;
        try {
            File f = new File(Play.applicationPath.getAbsolutePath(), "lib");
            Logger.debug("app path %s", f.getAbsolutePath());
            System.setProperty("java.library.path", f.getAbsolutePath());
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                throw new IOException("Error: Port is currently in use");
            } else {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (commPort instanceof CommPort) {
                    serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    in = serialPort.getInputStream();
                    out = serialPort.getOutputStream();

                } else {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            }
        } catch (Exception ex) {
            Logger.error("SerialPortAdapter : " + ex.getMessage());
            throw new IOException(String.format("Error initializing serial port %s", portName), ex);
        }
        (new Thread(new SerialReader())).start();
    }

    /**
     *
     */
    public class SerialReader implements Runnable {

        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = in.read(buffer)) > -1) {
                    for (int i = 0; i < len; i++) {
                        fifo.add(buffer[i]);
                    }
                }
            } catch (IOException e) {
                Logger.error("Glory error reading serial port");
            }
        }
    }

    public void close() throws IOException {
        try {
            Logger.debug(String.format("Closing serial port %s", serialPort.getName()));
            if (serialPort != null) {
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

    public byte read() throws IOException {
        Byte ch;
        try {
            ch = fifo.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Interrupt reading from port", e);
        }
        if (ch == null) {
            throw new IOException(String.format("Error reading from port %s", serialPort.getName()));
        }
        return ch;
    }
}
