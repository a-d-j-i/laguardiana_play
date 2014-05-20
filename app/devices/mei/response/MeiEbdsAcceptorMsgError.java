package devices.mei.response;

import devices.mei.MeiEbdsDevice.MessageType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgError implements MeiEbdsAcceptorMsgInterface {

    private final String error;

    public MeiEbdsAcceptorMsgError(String error) {
        this.error = error;
    }

    public MessageType getMessageType() {
        return MessageType.ERROR;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgError " + "error=" + error;
    }

}
