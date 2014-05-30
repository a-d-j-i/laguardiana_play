package devices.mei;

import devices.device.state.DeviceStateInterface;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgEnq;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.response.MeiEbdsAcceptorMsgInterface;
import devices.serial.SerialPortAdapterInterface;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import play.Logger;

public class MeiEbds {

    private final int readTimeout;
    private SerialPortAdapterInterface serialPort = null;
    int retries = 0;

    MeiEbds(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public synchronized boolean open(SerialPortAdapterInterface serialPort) {
        Logger.info("Opening mei serial port %s", serialPort);
        if (serialPort == null || this.serialPort != null) {
            return false;
        }
        this.serialPort = serialPort;
        return serialPort.open();
    }

    public synchronized void close() {
        Logger.info("Closing mei serial port ");
        if (serialPort != null) {
            serialPort.close();
        }
        serialPort = null;
    }

    public boolean write(MeiEbdsHostMsg operation) {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Logger.debug("Writting : %s", Arrays.toString(operation.getCmdStr()));
        return serialPort.write(operation.getCmdStr());
    }

    public Byte read(int timeout) throws TimeoutException, InterruptedException {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPort.read(timeout);
        if (ch == null) {
            throw new TimeoutException("timeout reading from port");
        }
//        Logger.debug("readed ch : 0x%x", ch);
        return ch;
    }

    public String sendMessage(final MeiEbdsHostMsg msg) {
        if (!write(msg)) {
            return "Error writting to the port";
        }
        Logger.debug("MEI sent msg : %s", msg.toString());
        return null;
    }

    public MeiEbdsAcceptorMsgInterface getMessage() throws InterruptedException {
        MeiEbdsAcceptorMsgInterface ret;
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
        Logger.debug("Received msg : %s == %s", ret.getMessageType().name(), ret.toString());
        return ret;
    }

    private MeiEbdsAcceptorMsgInterface getMessageInt() throws TimeoutException, InterruptedException {
        byte buffer[] = new byte[11];
        while (true) {
            Byte ch = read(readTimeout);
            switch (ch) {
                case 0x02: // stx
                    break;
                case 0x05: // enq
                    return new MeiEbdsAcceptorMsgEnq();
                default:
                    return new MeiEbdsAcceptorMsgError(String.format("mei invalid stx 0x%x  ", ch));
            }
            buffer[ 0] = ch;
            ch = read(120);
            byte length = ch;
            if (ch != 0x0b) {
                return new MeiEbdsAcceptorMsgError(String.format("mei invalid len %d", length));
            }
            buffer[ 1] = length;
            for (int i = 0; i < length - 2; i++) {
                ch = read(120);
                buffer[ i + 2] = ch;
            }
            if (!lastResult.setData(buffer)) {
                return new MeiEbdsAcceptorMsgError(String.format("mei invalid buffer data %s", Arrays.toString(buffer)));
            }
//            Logger.debug("readed data : %s == %s", Arrays.toString(buffer), result.toString());
            return lastResult;
        }
    }
    private final MeiEbdsAcceptorMsgAck lastResult = new MeiEbdsAcceptorMsgAck();
    private final MeiEbdsHostMsg hostMsg = new MeiEbdsHostMsg();

    public boolean count() {
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

    public DeviceStateInterface processMessageForCancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String cancelCount() {
        hostMsg.disableAllDenominations();
        return sendPollMessage();
    }

    public String sendPollMessage() {
        Logger.debug("MEI sent msg : %s", hostMsg.toString());
        String err = sendMessage(hostMsg);
        hostMsg.clearStackNote();
        hostMsg.clearReturnNote();
        return err;
    }

    public String checkAck(MeiEbdsAcceptorMsgAck msg) {
        if (msg != lastResult) {
            return "Error the only valid message is lastResutl";
        }
        if (msg.getAck() != hostMsg.getAck()) {
            return String.format("recived an nak message %s", msg);
        }
        Logger.debug("GOT AN ACK FOR HOSTPOOL, flipping ack");
        hostMsg.flipAck();
        return null;
    }
}
