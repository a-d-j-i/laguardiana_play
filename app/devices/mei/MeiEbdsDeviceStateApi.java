/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.device.DeviceStatus;
import devices.device.task.DeviceTaskInterface;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsg;
import devices.mei.state.MeiEbdsError;
import devices.mei.state.MeiEbdsStateOperation;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsDeviceStateApi {

    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final BlockingQueue<DeviceTaskInterface> queue;
    final private static int MEI_EBDS_READ_TIMEOUT = 35; //35ms
    private SerialPortAdapterInterface serialPort = null;

    MeiEbdsDeviceStateApi(BlockingQueue<DeviceTaskInterface> operationQueue) {
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

    public DeviceTaskInterface poll(int timeoutMS, TimeUnit timeUnit) throws InterruptedException {
        return queue.poll(timeoutMS, timeUnit);
    }

    public DeviceTaskInterface peek() {
        return queue.peek();
    }

    public boolean write(MeiEbdsHostMsg operation, boolean debug) {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        return serialPort.write(operation.getCmdStr());
    }

    public Byte read(int timeout) {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPort.read(timeout);
//        Logger.debug("readed ch : 0x%x", ch);
        return ch;
    }

    public MeiEbdsStateOperation exchangeMessage(final MeiEbdsHostMsg msg, final MeiEbdsAcceptorMsg result) {
        if (!write(msg, true)) {
            return new MeiEbdsError(this, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "Error writting to the port");
        }
        Logger.debug("MEI sent msg : %s", msg.toString());

        byte buffer[] = new byte[11];
        while (true) {
            Byte ch = read(120);
            if (ch == null) {
                Logger.debug("timeout reading from port");
                return null;
            } else if (ch != 0x02) {
                Logger.debug("mei invalid stx 0x%x  ", ch);
                return null;
            }
            buffer[ 0] = ch;
            ch = read(30);
            if (ch == null) {
                return new MeiEbdsError(this, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "error reading from port");
            }
            byte length = ch;
            if (ch != 0x0b) {
                Logger.debug("mei invalid len %d", length);
                continue;
            }
            buffer[ 1] = length;
            for (int i = 0; i < length - 2; i++) {
                ch = read(120);
                if (ch == null) {
                    return new MeiEbdsError(this, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "error reading from port");
                }
                buffer[ i + 2] = ch;
            }
            if (!result.setData(buffer)) {
                Logger.debug("mei invalid buffer data");
                continue;
            }
//            Logger.debug("readed data : %s == %s", Arrays.toString(buffer), result.toString());
            break;
        }
        return null;
    }

}
