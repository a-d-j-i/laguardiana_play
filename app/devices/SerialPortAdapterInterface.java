package devices;

import java.io.IOException;
import java.io.InputStream;

public interface SerialPortAdapterInterface {

    public void close() throws IOException;

    public void write(byte[] buffer) throws IOException;

    public byte read(int timeout) throws IOException;

    public String readLine(int timeout) throws IOException;

    public InputStream getInputStream();
}
