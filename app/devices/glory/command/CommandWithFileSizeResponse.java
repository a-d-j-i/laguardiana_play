package devices.glory.command;

public class CommandWithFileSizeResponse extends CommandWithDataResponse {

    int fileSize = -1;

    CommandWithFileSizeResponse(byte cmdId, String description) {
        super(cmdId, description);
    }

    @Override
    public CommandWithFileSizeResponse setResult(byte[] dr) {
        super.setResult(dr);
        if (getError() != null) {
            return this;
        }

        if (getData() == null) {
            setError("Data is null");
            return this;
        }
        if (getData().length != 8) {
            setError(String.format("Invalid command (%s) response length %d expected 8 bytes hex number",
                    getDescription(), dr.length));
            return this;
        }
        byte[] b = getData();
        int l = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0x30 && b[i] <= 0x3F) {
                l += (b[i] - 0x30) * Math.pow(16, b.length - i - 1);
            } else {
                setError(String.format("Invalid digit %d == 0x%x", b[i], b[i]));
            }
        }
        fileSize = l;
        return this;
    }

    public int getFileSize() {
        return fileSize;
    }
}
