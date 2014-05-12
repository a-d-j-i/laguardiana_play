package devices.serial;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import play.Logger;

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
        BAUDRATE_38400(38400),
        BAUDRATE_57600(57600),
        BAUDRATE_115200(115200),
        BAUDRATE_128000(128000),
        BAUDRATE_256000(256000),;

        static PORTSPEED getBaudRate(String baudRate) {
            if (baudRate != null) {
                for (PORTSPEED ps : PORTSPEED.values()) {
                    if (baudRate.equalsIgnoreCase(Integer.toString(ps.q))) {
                        return ps;
                    }
                }
            }
            // Default.
            return PORTSPEED.BAUDRATE_115200;
        }
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
    protected PortConfiguration conf;
    protected String portName = null;

    public SerialPortAdapterAbstract(PortConfiguration conf) {
        this.conf = conf;
    }

    abstract public boolean open();

    abstract public void close();

    abstract public boolean write(byte[] buffer);

    enum State {

        START,
        CR_DETECTED,;
    }

    private final BlockingQueue< Byte> fifo = new ArrayBlockingQueue< Byte>(1024);

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

    protected void fifoAdd(byte b) {
        fifo.add(b);
    }

    public Byte read() {
        return fifo.poll();
    }

    public Byte read(int timeoutMS) {
        try {
            return fifo.poll(timeoutMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.error("Interrupt exception reading from fifo %s", ex);
            return null;
        }
    }

    // Not completly thread safe!!!, fails if used with read...
    public String readLine(int timeoutMS) {
        SerialPortAdapterAbstract.State state = SerialPortAdapterAbstract.State.START;
        StringBuilder sb = new StringBuilder(1024);
        boolean done = false;
        while (!done) {
            switch (state) {
                case START:
                    // remove from queue
                    int ch = read(timeoutMS);
                    if (ch == 0x0d) {
                        state = SerialPortAdapterAbstract.State.CR_DETECTED;
                    } else if (ch == 0x0a) {
                        done = true;
                    } else {
                        sb.append(ch);
                    }
                    break;
                case CR_DETECTED:
                    int b = read(timeoutMS);
                    if (b == 0x0a) {
                        done = true;
                    } else {
                        Logger.error("Invalid character received %s", b);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Serial port : " + portName;
    }

}
