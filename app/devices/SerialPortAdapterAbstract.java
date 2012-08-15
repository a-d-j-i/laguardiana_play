package devices;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class SerialPortAdapterAbstract implements SerialPortAdapterInterface {

    String portName = null;
    ArrayBlockingQueue< Byte> fifo = new ArrayBlockingQueue< Byte>(1024);

    abstract public void close() throws IOException;

    abstract public void write(byte[] buffer) throws IOException;

    public byte read() throws IOException {
        Byte ch;
        try {
            ch = fifo.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("SerialAdapter Interrupt reading from port", e);
        }
        if (ch == null) {
            throw new IOException("SerialAdapter Error reading from port");
        }
        //play.Logger.debug("Readed 0x%x", ch);
        return ch;
    }

    public InputStream getInputStream() {
        return new SerialInputStream();
    }

    class SerialInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            Byte ch;
            try {
                ch = fifo.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IOException("SerialInputStream Interrupt reading from port", e);
            }
            if (ch == null) {
                throw new IOException("SerialInputStream read  fifo empty");
            }
            return ch;
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
