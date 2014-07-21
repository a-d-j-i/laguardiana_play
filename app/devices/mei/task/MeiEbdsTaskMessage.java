package devices.mei.task;

import devices.device.DeviceMessageInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.mei.operation.MeiEbdsHostMsg;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author adji
 */
public class MeiEbdsTaskMessage extends DeviceTaskAbstract {

    public interface ResponseSubType {

        public int getId();

    };

    public enum ExtendedResponseSubType implements ResponseSubType {

        BarcodeData(0x01),
        RequestSupportedNoteSet(0x02),
        SetExtendedNoteInhibits(0x03),
        SetEscrowTimeouts(0x04);

        private final int id;

        private ExtendedResponseSubType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    };

    public enum ResponseType {

        HostToAcceptor(0x10),
        AcceptorToHost(0x20),
        BookmarkSelected(0x30),
        CalibrateMode(0x40),
        FlashDownload(0x50),
        Request(0x60),
        Extended(0x70),
        ENQ(0x100),
        Error(0x1000);

        static {
            for (ResponseSubType mt : ExtendedResponseSubType.values()) {
                Extended.msgSubTypeMap.put(mt.getId(), mt);
            }
        }

        final private Map<Integer, ResponseSubType> msgSubTypeMap = new HashMap<Integer, ResponseSubType>();
        final private int id;

        private ResponseType(int id) {
            this.id = id;
        }

        public ResponseSubType getSubType(int subtypeId) {
            return msgSubTypeMap.get(subtypeId);
        }

        public int getId() {
            return id;
        }

    };

    private final MeiEbdsHostMsg message;
    private final DeviceMessageInterface response;

    public MeiEbdsTaskMessage(MeiEbdsHostMsg msg, DeviceMessageInterface response) {
        this.message = msg;
        this.response = response;
    }

    public DeviceMessageInterface getResponse() {
        return response;
    }

    public MeiEbdsHostMsg getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MeiEbdsTaskMessage{" + "message=" + message + ", response=" + response + '}';
    }

}
