package devices.mei;

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

    public MeiEbds(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public synchronized boolean open(SerialPortAdapterInterface serialPort) {
        Logger.info("Opening glory serial port %s", serialPort);
        if (serialPort == null || this.serialPort != null) {
            return false;
        }
        this.serialPort = serialPort;
        return serialPort.open();
    }

    public synchronized void close() {
        Logger.info("Closing glory serial port ");
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

    public Byte read(int timeout) throws TimeoutException {
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

    public MeiEbdsAcceptorMsgInterface getMessage(final MeiEbdsAcceptorMsgAck result) throws TimeoutException {
        byte buffer[] = new byte[11];
        while (true) {
            Byte ch = read(10000);
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
            if (!result.setData(buffer)) {
                return new MeiEbdsAcceptorMsgError(String.format("mei invalid buffer data %s", Arrays.toString(buffer)));
            }
//            Logger.debug("readed data : %s == %s", Arrays.toString(buffer), result.toString());
            return result;
        }
    }

}
