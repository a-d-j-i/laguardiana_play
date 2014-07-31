package devices.glory.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class GloryDE50AcceptorMsg implements DeviceResponseInterface {

    private final byte[] data;
    private final int length;

    public GloryDE50AcceptorMsg(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "GloryDE50AcceptorMsg";
    }

    public String toString1() {
        if (data == null || length == 0) {
            return "Empty data";
        }
        StringBuilder hexString = new StringBuilder();
        hexString.append("\n---------------\n");
        for (int i = 0; i < length; i++) {
            hexString.append(" ").append(String.format("%2x", 0xFF & data[i]));
        }
        hexString.append("\n");
        for (int i = 0; i < length; i++) {
            if (data[i] > 31 && data[i] < 127) {
                hexString.append("  ").append((char) (0xFF & data[i]));
            } else {
                hexString.append(" ").append(String.format("%2d", 0xFF & data[i]));
            }
        }
        hexString.append("\n---------------\n");
        return "GloryDE50AcceptorMsg " + hexString.toString();
    }

}
