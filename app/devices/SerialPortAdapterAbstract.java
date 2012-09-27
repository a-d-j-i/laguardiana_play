package devices;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SerialPortAdapterAbstract implements SerialPortAdapterInterface {

    public enum PORTSPEED {

        BAUDRATE_110(110),
        BAUDRATE_300(300),
        BAUDRATE_600(600),
        BAUDRATE_1200(1200),
        BAUDRATE_4800(4800),
        BAUDRATE_9600(9600),
        BAUDRATE_14400(14400),
        BAUDRATE_19200(19200),
        BAUDRATE_57600(57600),
        BAUDRATE_115200(115200),
        BAUDRATE_128000(128000),
        BAUDRATE_256000(256000),;
        private int q;

        private PORTSPEED(int q) {
            this.q = q;
        }

        public int getQ() {
            return q;
        }
    }

    public enum PORTBITS {

        BITS_5(5),
        BITS_6(6),
        BITS_7(7),
        BITS_8(8);
        private int q;

        private PORTBITS(int q) {
            this.q = q;
        }

        public int getQ() {
            return q;
        }
    }

    public enum PORTSTOPBITS {

        STOP_BITS_1(1),
        STOP_BITS_2(2),
        STOP_BITS_1_5(3),;
        private int q;

        private PORTSTOPBITS(int q) {
            this.q = q;
        }

        public int getQ() {
            return q;
        }
    }

    public enum PORTPARITY {

        PARITY_NONE(0),
        PARITY_ODD(1),
        PARITY_EVEN(2),
        PARITY_MARK(3),
        PARITY_SPACE(4),;
        private int q;

        private PORTPARITY(int q) {
            this.q = q;
        }

        public int getQ() {
            return q;
        }
    }

    static public class PortConfiguration {

        public PORTSPEED speed = PORTSPEED.BAUDRATE_9600;
        public PORTBITS bits = PORTBITS.BITS_7;
        public PORTSTOPBITS stop_bits = PORTSTOPBITS.STOP_BITS_1;
        public PORTPARITY parity = PORTPARITY.PARITY_EVEN;

        public PortConfiguration(PORTSPEED speed, PORTBITS bits, PORTSTOPBITS stop_bits, PORTPARITY parity) {
            this.speed = speed;
            this.bits = bits;
            this.stop_bits = stop_bits;
            this.parity = parity;
        }
    }
    PortConfiguration conf;
    String portName = null;
    private ArrayBlockingQueue< Byte> fifo = new ArrayBlockingQueue< Byte>(1024);

    public SerialPortAdapterAbstract(PortConfiguration conf) {
        this.conf = conf;
    }

    public void reconect() throws IOException {
        close();
        open();
    }

    abstract protected void open() throws IOException;

    abstract public void close() throws IOException;

    abstract public void write(byte[] buffer) throws IOException;

    enum State {

        START,
        CR_DETECTED,;
    }

    protected void fifoAdd(byte b) {
        fifo.add(b);
    }

    public byte read(int timeout) throws IOException {
        Byte ch;
        try {
            ch = fifo.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IOException("SerialAdapter Interrupt Exception");
        }
        if (ch == null) {
            throw new IOException(new TimeoutException(String.format("timeout : %d", timeout)));
        }
        return ch;
    }

    // Not completly thread safe!!!, fails if used with read...
    public String readLine(int timeout) throws IOException {
        State state = State.START;
        StringBuilder sb = new StringBuilder(1024);
        boolean done = false;
        while (!done) {
            switch (state) {
                case START:
                    // remove from queue
                    byte ch = read(timeout);
                    if (ch == 0x0d) {
                        state = State.CR_DETECTED;
                    } else if (ch == 0x0a) {
                        done = true;
                    } else {
                        sb.append((char) ch);
                    }
                    break;
                case CR_DETECTED:
                    done = true;
                    Byte b = null;
                    long startT = (new Date()).getTime();
                    while (((new Date()).getTime() - startT) < timeout && b == null) {
                        b = fifo.peek();
                        if (b == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                    if (b == null) {
                        // Timeout
                        throw new IOException(new TimeoutException(String.format("timeout 2 : %d", timeout)));
                    } else {
                        if (b == 0x0a) {
                            // remove from queue
                            read(timeout);
                        }
                    }
                    break;
            }
        }

        return sb.toString();
    }

    public InputStream getInputStream() {
        return new SerialInputStream();


    }

    class SerialInputStream extends InputStream {

        final int SERIAL_INPUT_STREAM_TIMEOUT = 60000;

        @Override
        public int read() throws IOException {
            return SerialPortAdapterAbstract.this.read(SERIAL_INPUT_STREAM_TIMEOUT);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.portName != null ? this.portName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SerialPortAdapterAbstract other = (SerialPortAdapterAbstract) obj;
        if ((this.portName == null) ? (other.portName != null) : !this.portName.equals(other.portName)) {
            return false;
        }
        return true;
    }
}
