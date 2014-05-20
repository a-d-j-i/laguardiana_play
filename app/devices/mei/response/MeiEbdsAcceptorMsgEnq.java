package devices.mei.response;

import devices.mei.MeiEbdsDevice.MessageType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgEnq implements MeiEbdsAcceptorMsgInterface {

    public MeiEbdsAcceptorMsgEnq() {
    }

    public MessageType getMessageType() {
       return MessageType.ENQ;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgEnq";
    }

}
