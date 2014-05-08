package devices.serial;

public interface SerialPortAdapterInterface {

    public boolean write(byte[] buffer);

    public boolean open();

    public void close();

    public Byte read();

    public Byte read(int timeout);

    public String readLine(int timeout);
}