/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

/**
 *
 * @author adji
 */
class MeiEbdsAcceptorMsg {

    enum MessageType {

        HostToAcceptor(1),
        AcceptorToHost(2),
        BookmarkSelected(3),
        CalibrateMode(4),
        FlashDownload(5),
        Request(6),
        Extended(7);

        private static MessageType get(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private int id;

        private MessageType(int id) {
            this.id = id;
        }
    };

    final public MessageType messageType;
    final public boolean ack;
    final byte[] data;

    public MeiEbdsAcceptorMsg(byte[] buffer) {
        byte t = buffer[ 0];
        messageType = MessageType.get((t & 0xF0) >> 4);
        ack = ((t & 0x0F) == 0);
        data = buffer;
    }
}
