package devices.mei;

import devices.device.DeviceMessageInterface;
import devices.device.DeviceStatusInterface;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgEnq;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import devices.serial.SerialPortReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import models.Configuration;
import play.Logger;

public class MeiEbds implements SerialPortMessageParserInterface {

    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(
            SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
            SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final private static int MEI_EBDS_READ_TIMEOUT = 30000; //35ms

    private SerialPortReader serialPortReader = null;
    final MeiEbdsDevice.MeiApi api;
    int retries = 0;

    MeiEbds(MeiEbdsDevice.MeiApi api) {
        this.api = api;
    }

    public void notifyListeners(DeviceStatusInterface status) {
        api.notifyListeners(status);
    }

    public synchronized boolean open(String port) {
        Logger.debug("api open");
        close();
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(port, portConf);
        if (serialPort == null || this.serialPortReader != null) {
            Logger.info("Error opening mei serial port %s %s", serialPort, this.serialPortReader);
            return false;
        }
        this.serialPortReader = new SerialPortReader(serialPort, this, api);
        return serialPortReader.open();
    }

    public synchronized void close() {
        if (serialPortReader != null) {
            Logger.info("Closing mei serial port ");
            serialPortReader.close();
            serialPortReader = null;
        }
    }

    public DeviceMessageInterface getMessage(SerialPortAdapterInterface serialPort) throws InterruptedException {
        DeviceMessageInterface ret;
        try {
            ret = getMessageInt();
        } catch (TimeoutException ex) {
            Logger.debug("Timeout waiting for device, retry");
            //pool the machine.
            if (retries++ > 100) {
                return new MeiEbdsAcceptorMsgError("Timeout reading from port");
            }
            String err = sendPollMessage();
            if (err != null) {
                return new MeiEbdsAcceptorMsgError(err);
            }
            return null;
        }
        retries = 0;
        Logger.debug("Received msg : %s == %s", ret.getType().name(), ret.toString());
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
                    Logger.error(String.format("mei invalid stx 0x%x  ", ch));
                    continue;
            }
            buffer[ 0] = ch;
            ch = read(120);
            byte length = ch;
            buffer[ 1] = length;
            for (int i = 0; i < Math.min(length, buffer.length) - 2; i++) {
                ch = read(120);
                if (ch == null) {
                    return new MeiEbdsAcceptorMsgError(String.format("mei invalid len %d and timeout reading", length));
                }
                buffer[ i + 2] = ch;
            }
            switch (length) {
                case 0x0b: // normal message
                case 30: // extendend message
                    // TODO: I must check the checksum first and retry.
                    if (!lastResult.setData(length, buffer)) {
                        return new MeiEbdsAcceptorMsgError(String.format("mei invalid buffer data %s", Arrays.toString(buffer)));
                    }
                    return lastResult;
                case 8: // echo of my messages, retry.
                    return new MeiEbdsAcceptorMsgEnq();
                default:
                    String err = String.format("mei invalid buffer len %d data %s", length, Arrays.toString(buffer));
                    Logger.error(err);
                    //return new MeiEbdsAcceptorMsgError(err);
                    // with mei shit happens, retry.
                    return new MeiEbdsAcceptorMsgEnq();
            }
        }
    }
    private final MeiEbdsAcceptorMsgAck lastResult = new MeiEbdsAcceptorMsgAck();
    private final MeiEbdsHostMsg hostMsg = new MeiEbdsHostMsg();

    public boolean count(List<Integer> slotList) {
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
        Logger.debug("MEI sending msg : %s", hostMsg.toString());
        if (serialPortReader == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Logger.debug("Writting : %s", Arrays.toString(hostMsg.getCmdStr()));
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
        Logger.debug("GOT AN ACK FOR HOSTPOOL, flipping ack");
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
//        Logger.debug("readed ch : 0x%x", ch);
        return ch;
    }

}
