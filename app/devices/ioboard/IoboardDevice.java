package devices.ioboard;

import devices.device.DeviceSerialPortAbstract;
import devices.device.state.DeviceStateInterface;
import devices.ioboard.state.IoboardOpenPort;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortMessageParserInterface;
import play.Logger;

/**
 *
 * @author adji
 */
final public class IoboardDevice extends DeviceSerialPortAbstract {

    protected void debug(String message, Object... args) {
        //    Logger.debug(message, args);
    }

    public static final int IOBOARD_MAX_RETRIES = 3;
    public static final int IOBOARD_READ_TIMEOUT = 1000;

    static public enum IoBoardDeviceType {

        IOBOARD_DEVICE_TYPE_MEI_1_0 {

                    @Override
                    public SerialPortMessageParserInterface getParser() {
                        return new IoboardDeviceMei_1_0Parser();
                    }

                    @Override
                    public SerialPortAdapterAbstract.PortConfiguration getPortConfiguration() {
                        return new SerialPortAdapterAbstract.PortConfiguration(
                                SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_38400, SerialPortAdapterAbstract.PORTBITS.BITS_8,
                                SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_NONE);
                    }
                };

        abstract public SerialPortMessageParserInterface getParser();

        abstract public SerialPortAdapterAbstract.PortConfiguration getPortConfiguration();
    }

    public IoboardDevice(IoBoardDeviceType type) {
        super(type.getParser(), type.getPortConfiguration());
    }

    public String sendCmd(char cmd) {
        byte[] b = new byte[1];
        b[0] = (byte) cmd;
        if (cmd != 'S') {
            Logger.debug("IoBoard writting %c", cmd);
        }
        if (!write(b)) {
            return String.format("IoBoard Error writing to port");
        }
        return null;
    }

    @Override
    public DeviceStateInterface getInitState() {
        return new IoboardOpenPort(this);
    }

    @Override
    public String toString() {
        return "IoboardDevice ( " + super.toString() + " )";
    }

}
