/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.device.DeviceStatus;
import devices.device.task.DeviceTaskAbstract;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsg;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsDeviceStateApi {

    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final BlockingQueue<DeviceTaskAbstract> queue;
    final private static int MEI_EBDS_READ_TIMEOUT = 35; //35ms
    private SerialPortAdapterInterface serialPort = null;

    MeiEbdsDeviceStateApi(BlockingQueue<DeviceTaskAbstract> operationQueue) {
        this.queue = operationQueue;
    }

    boolean closing = false;

    public void notifyListeners(String details) {
    }

    public void notifyListeners(DeviceStatus.STATUS status) {
    }

    public boolean open(String value) {
        if (serialPort != null) {
            Logger.debug("close port %s", serialPort);
            serialPort.close();
            Logger.info(String.format("Configuring serial port %s", serialPort));
        }
        serialPort = Configuration.getSerialPort(value, portConf);
        Logger.debug("Mei port open try for %s == %s", value, serialPort);
        boolean ret = serialPort.open();
        Logger.debug("Mei port open : %s", ret ? "success" : "fails");
        return ret;
    }

    void close() {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    public DeviceTaskAbstract poll(int timeoutMS, TimeUnit timeUnit) throws InterruptedException {
        return queue.poll(timeoutMS, timeUnit);
    }

    public DeviceTaskAbstract poll() {
        return queue.poll();
    }

    public boolean write(MeiEbdsHostMsg operation) {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        return serialPort.write(operation.getCmdStr());
    }

    public Byte read(int timeout) throws TimeoutException {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPort.read(timeout);
        if (ch == null) {
            Logger.debug("ch is null");
            throw new TimeoutException("timeout reading from port");
        }
        Logger.debug("readed ch : 0x%x", ch);
        return ch;
    }

    public String sendMessage(final MeiEbdsHostMsg msg) {
        if (!write(msg)) {
            return "Error writting to the port";
        }
        Logger.debug("MEI sent msg : %s", msg.toString());
        return null;
    }

    public String getMessage(final MeiEbdsAcceptorMsg result) throws TimeoutException {
        byte buffer[] = new byte[11];
        while (true) {
            Byte ch = read(1200);
            if (ch != 0x02) {
                return String.format("mei invalid stx 0x%x  ", ch);
            }
            buffer[ 0] = ch;
            ch = read(120);
            byte length = ch;
            if (ch != 0x0b) {
                return String.format("mei invalid len %d", length);
            }
            buffer[ 1] = length;
            for (int i = 0; i < length - 2; i++) {
                ch = read(120);
                buffer[ i + 2] = ch;
            }
            if (!result.setData(buffer)) {
                return String.format("mei invalid buffer data %s", Arrays.toString(buffer));
            }
//            Logger.debug("readed data : %s == %s", Arrays.toString(buffer), result.toString());
            return null;
        }
    }
}
