package devices.mei;

import devices.device.DeviceMessageInterface;
import devices.device.DeviceMessageListenerInterface;
import devices.device.status.DeviceStatusInterface;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgEnq;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import devices.serial.SerialPortReader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import models.Configuration;
import play.Logger;

public class MeiEbds implements SerialPortMessageParserInterface, DeviceMessageListenerInterface {

    private void debug(String message, Object... args) {
        //Logger.debug(message, args);
    }
    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(
            SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
            SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final private static int MEI_EBDS_READ_TIMEOUT = 3000; //35ms

    private SerialPortReader serialPortReader = null;
    final MeiEbdsDevice.MeiApi api;
    int retries = 0;

    public MeiEbds(MeiEbdsDevice.MeiApi api) {
        this.api = api;
    }

    public void onDeviceMessageEvent(DeviceMessageInterface msg) {
        api.onDeviceMessageEvent(hostMsg, msg);
    }

    public void notifyListeners(DeviceStatusInterface status) {
        api.notifyListeners(status);
    }

    private String port;

    public synchronized boolean open(String port) {
        debug("%s api open", this.toString());
        this.port = port;
        close();
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(port, portConf);
        if (serialPort == null || this.serialPortReader != null) {
            Logger.info("%s Error opening mei serial port %s %s", this.toString(), serialPort, this.serialPortReader);
            return false;
        }
        this.serialPortReader = new SerialPortReader(serialPort, this, this);
        return serialPortReader.open();
    }

    public synchronized void close() {
        if (serialPortReader != null) {
            Logger.info("%s Closing mei serial port ", this.toString());
            serialPortReader.close();
            serialPortReader = null;
        }
    }

    public DeviceMessageInterface getMessage(SerialPortAdapterInterface serialPort) throws InterruptedException {
        DeviceMessageInterface ret;
        try {
            ret = getMessageInt();
        } catch (TimeoutException ex) {
            debug("%s Timeout waiting for device, retry", this.toString());
            //pool the machine.
            if (retries++ > 100) {
                return new MeiEbdsAcceptorMsgError(String.format("%s Timeout reading from port", this.toString()));
            }
            String err = sendPollMessage();
            if (err != null) {
                return new MeiEbdsAcceptorMsgError(err);
            }
            return null;
        }
        retries = 0;
        debug("%s Received msg : %s == %s", this.toString(), ret.getType().name(), ret.toString());
        return ret;
    }

    private DeviceMessageInterface getMessageInt() throws TimeoutException, InterruptedException {
        byte buffer[] = new byte[40];
        while (true) {
            Byte ch = read(MEI_EBDS_READ_TIMEOUT);
            switch (ch) {
                case 0x02: // stx
                    break;
                case 0x05: // enq
                    return new MeiEbdsAcceptorMsgEnq();
                default: // keep trying
                    Logger.error("%s mei invalid stx 0x%x  ", this.toString(), ch);
                    if (ch == 0) {
                        Thread.sleep(300);
                    }
                    continue;
            }
            buffer[ 0] = ch;
            ch = read(120);
            byte length = ch;
            buffer[ 1] = length;
            for (int i = 0; i < Math.min(length, buffer.length) - 2; i++) {
                ch = read(120);
                if (ch == null) {
                    return new MeiEbdsAcceptorMsgError(String.format("%s mei invalid len %d and timeout reading", this.toString(), length));
                }
                buffer[ i + 2] = ch;
            }
            switch (length) {
                case 0x0b: // normal message
                case 30: // extendend message
                    // TODO: I must check the checksum first and retry.
                    if (!lastResult.setData(length, buffer)) {
                        return new MeiEbdsAcceptorMsgError(String.format("%s mei invalid buffer data %s", this.toString(), Arrays.toString(buffer)));
                    }
                    return lastResult;
                case 8: // echo of my messages, retry.
                    return new MeiEbdsAcceptorMsgEnq();
                default:
                    String err = String.format("%s mei invalid buffer len %d data %s", this.toString(), length, Arrays.toString(buffer));
                    Logger.error(err);
                    //return new MeiEbdsAcceptorMsgError(err);
                    // with mei shit happens, retry.
                    return new MeiEbdsAcceptorMsgEnq();
            }
        }
    }
    private final MeiEbdsAcceptorMsgAck lastResult = new MeiEbdsAcceptorMsgAck();
    private final MeiEbdsHostMsg hostMsg = new MeiEbdsHostMsg();

    public boolean count(Map<String, Integer> desiredQuantity) {
        // TODO: Implement slotlist
        hostMsg.enableAllDenominations();
        return true;
    }

    public boolean store() {
        if (!lastResult.isEscrowed()) {
            return false;
        }
        hostMsg.setStackNote();
        return true;
    }

    public boolean reject() {
        if (!lastResult.isEscrowed()) {
            return false;
        }
        hostMsg.setReturnNote();
        return true;
    }

    public String cancelCount() {
        hostMsg.disableAllDenominations();
        return sendPollMessage();
    }

    public String sendPollMessage() {
        String err = null;
        debug("%s MEI sending msg : %s", this.toString(), hostMsg.toString());
        if (serialPortReader == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        debug("%s Writting : %s", this.toString(), Arrays.toString(hostMsg.getCmdStr()));
        if (!serialPortReader.write(hostMsg.getCmdStr())) {
            err = "Error writting to the port";
        }
        hostMsg.clearStackNote();
        hostMsg.clearReturnNote();
        return err;
    }

    public boolean isAckOk(MeiEbdsAcceptorMsgAck msg) {
        if (msg.getAck() != hostMsg.getAck()) { // repeated message, ignore
            //return String.format("recived an nak message %s", msg);
            return false;
        }
        //debug("%s GOT AN ACK FOR HOSTPOOL, flipping ack", this.toString());
        hostMsg.flipAck();
        return true;
    }

    public boolean isMessageOk(DeviceMessageInterface msg) {
        return (msg == lastResult);
    }

    private Byte read(int timeout) throws TimeoutException, InterruptedException {
        if (serialPortReader == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPortReader.read(timeout);
        if (ch == null) {
            throw new TimeoutException("timeout reading from port");
        }
//        debug("readed ch : 0x%x", ch);
        return ch;
    }

    @Override
    public String toString() {
        return "MeiEbds{" + "port=" + port + '}';
    }

}
