package devices.glory.command;

public class CommandWithFileSizeResponse extends CommandWithDataResponse {

    Long fileSize = new Long(-1);

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
        fileSize = Long.parseLong(new String(getData()), 16);
        return this;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
