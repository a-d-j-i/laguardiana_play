package devices.device;

import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReadTimeout;
import devices.device.task.DeviceTaskResponse;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import java.util.Arrays;
import java.util.List;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class DeviceSerialPortAbstract extends DeviceAbstract {

    private void debug(String message, Object... args) {
        //Logger.debug(message, args);
    }

    private DeviceSerialPortAdaptor serialPortReader = null;
    private String port;
    private final SerialPortMessageParserInterface parser;
    private final SerialPortAdapterAbstract.PortConfiguration portConf;
    private final DeviceResponseListenerInterface responseListener = new DeviceResponseListenerInterface() {

        // switch thread.
        public void onDeviceMessageEvent(final DeviceResponseInterface response) {
            if (response == null) {
                submit(new DeviceTaskReadTimeout());
            } else {
                submit(new DeviceTaskResponse(response));
            }
        }
    };

    protected boolean write(byte[] cmdStr) {
        if (serialPortReader == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        debug("%s Writting : %s", this.toString(), Arrays.toString(cmdStr));
        return serialPortReader.write(cmdStr);
    }

    public DeviceSerialPortAbstract(SerialPortMessageParserInterface parser, SerialPortAdapterAbstract.PortConfiguration portConf) {
        this.parser = parser;
        this.portConf = portConf;
    }

    @Override
    public List<String> getNeededProperties() {
        return Arrays.asList(new String[]{"port"});
    }

    @Override
    public boolean setProperty(String property, String value) {
        debug("trying to set property %s to %s", property, value);
        if (property.compareToIgnoreCase("port") == 0) {
            boolean ret = submitSynchronous(new DeviceTaskOpenPort(value));
            debug("changing port to %s %s", value, ret ? "SUCCESS" : "FAIL");
            return ret;
        }
        return false;
    }

    public synchronized boolean open(String port) {
        this.port = port;
        debug("%s api open", this.toString());
        close();
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(port, portConf);
        if (serialPort == null || this.serialPortReader != null) {
            Logger.info("%s Error opening mei serial port %s %s", this.toString(), serialPort, this.serialPortReader);
            return false;
        }
        debug("%s creating adapter", this.toString());
        serialPortReader = new DeviceSerialPortAdaptor(serialPort, parser, responseListener);
        debug("%s calling adapter.open", this.toString());
        return serialPortReader.open();
    }

    public synchronized void close() {
        if (serialPortReader != null) {
            Logger.info("%s Closing serial port ", this.toString());
            serialPortReader.close();
            serialPortReader = null;
        }
    }

    @Override
    public void finish() {
        debug("Executing finish");
        close();
    }

    @Override
    public String toString() {
        return "port=" + port;
    }

}
