package devices.mei;

import devices.device.DeviceResponseInterface;
import devices.device.DeviceSerialPortAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskMessage;
import devices.device.task.DeviceTaskResponse;
import devices.mei.state.MeiEbdsOpenPort;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.serial.SerialPortAdapterAbstract;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MeiEbdsDevice extends DeviceSerialPortAbstract {

    final public static int MEI_EBDS_MAX_RETRIES = 100;
    final public static int MEI_EBDS_READ_TIMEOUT = 500; //35ms

    public MeiEbdsDevice() {
        super(new MeiEbdsParser(), new SerialPortAdapterAbstract.PortConfiguration(
                SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
                SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN)
        );
    }

    @Override
    public DeviceStateInterface getInitState() {
        return new MeiEbdsOpenPort(this);
    }
    private MeiEbdsAcceptorMsgAck lastResult = new MeiEbdsAcceptorMsgAck();
    private final MeiEbdsHostMsg hostMsg = new MeiEbdsHostMsg();

    @Override
    protected void runTask(final DeviceTaskAbstract deviceTask) {
        if (deviceTask instanceof DeviceTaskResponse) {
            DeviceTaskResponse r = (DeviceTaskResponse) deviceTask;
            DeviceResponseInterface response = r.getResponse();
            if (response instanceof MeiEbdsAcceptorMsgAck) {
                lastResult = (MeiEbdsAcceptorMsgAck) response;
            }
            deviceTask.setReturnValue(true);
            super.runTask(new DeviceTaskMessage(hostMsg, response));
        } else {
            super.runTask(deviceTask);
        }
    }

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
        Logger.debug("%s MEI sending msg : %s", this.toString(), hostMsg.toString());
        if (!write(hostMsg.getCmdStr())) {
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
        //Logger.debug("%s GOT AN ACK FOR HOSTPOOL, flipping ack", this.toString());
        hostMsg.flipAck();
        return true;
    }

    public boolean isMessageOk(DeviceResponseInterface msg) {
        return (msg == lastResult);
    }

    public boolean isSomeDenominationEnabled() {
        return hostMsg.isSomeDenominationEnabled();
    }

    @Override
    public String toString() {
        return "MeiEbdsDevice ( " + super.toString() + " )";
    }

}
