package devices.serial.implementations;

import devices.serial.SerialPortAdapterAbstract;
import jssc.*;
import play.Logger;

public final class SerialPortAdapterJSSC extends SerialPortAdapterAbstract implements SerialPortEventListener {

    SerialPort serialPort;

    public void serialEvent(SerialPortEvent event) {
        if (serialPort == null) {
            Logger.error("Error reading serial port, port closed");
            return;
        }
        if (event.isRXCHAR()) {
            try {
                for (byte b : serialPort.readBytes()) {
                    fifoAdd(b);
                }
            } catch (SerialPortException e) {
                Logger.error("Error reading serial port %s", e.getMessage());
            }
        }
    }

    public SerialPortAdapterJSSC(String portN, PortConfiguration conf) {
        super(conf);
        portName = portN;
    }

    @Override
    public boolean open() {
        try {
            String[] ports = SerialPortList.getPortNames();
            Integer p = Integer.parseInt(portName);
            if (p < ports.length) {
                portName = ports[p];
            }
        } catch (NumberFormatException e) {
        }

        Logger.debug(String.format("Configuring serial port %s", portName));
        serialPort = new SerialPort(portName);
        if (serialPort == null) {
            Logger.error(String.format("SerialPortAdapter : error oppening serial port %s", portName));
            return false;
        }
        try {
            Logger.debug(String.format("Opening serial port %s", portName));
            serialPort.openPort();
            serialPort.setParams(conf.speed.getQ(), conf.bits.getQ(), conf.stop_bits.getQ(), conf.parity.getQ());
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            Logger.error("SerialPortAdapter : " + e.getMessage());
            return false;
        }
        return true;
    }

    public void close() {
        try {
            Logger.debug(String.format("Closing serial port %s", serialPort.getPortName()));
            if (serialPort != null) {
                serialPort.closePort();
                serialPort = null;
            }
        } catch (Exception e) {
            Logger.error(String.format("Error closing serial port"), e);
        }
    }

    public boolean write(byte[] buffer) {
        if (serialPort == null) {
            Logger.debug("Error writing to serial port, port closed");
            return false;
        }
        try {
            serialPort.writeBytes(buffer);
        } catch (SerialPortException e) {
            Logger.debug(String.format("Error writing to serial port %s, %s", serialPort.getPortName(), e));
            return false;
        }
        return true;
    }

}
