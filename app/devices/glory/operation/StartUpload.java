package devices.glory.operation;

import devices.glory.response.GloryDE50ResponseWithData;


/*
 * Upload a file from DE to TM. If DE have file with same name, DE delete it.
 * NAK is returned when file access is failure. Refer to the Appendix 2 for the
 * file name.
 */
public class StartUpload extends OperationdWithDataResponse {

    public enum Files {

        ERROR_T("The log of error that occurs in time series"),
        ERROR_C("The number of error code , which individual items are sorted by error code."),
        REJECT_INFO("The number of rejected notes"),
        STATUS_INFO("The log of transition of status Reserve"),
        WARNING_INFO("The number of retry process"),
        RUNNING_INFO("The log of running information"),
        TRANSACTION("The log of transaction"),
        DEVICE_VERSION("Program version number"),
        DEVICE_SETTING("The log of setting data"),
        INITIALIZATION_INFO("The date of cleared memory"),
        OPERATION("The log of users operation"),
        COUNTER_INFO("The log of notes counter"),
        DEPOSIT_DATA("The log of deposit data Reserve"),
        ONLINE_DATA("The log of online communication For DE-100"),
        TRACE_LOG("The log of mechanical trace"),;
        private String description;

        private Files(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    final Files fileName;

    public StartUpload(Files fileName) {
        super(0x52);
        this.fileName = fileName;
    }

    @Override
    public byte[] getCmdStr() {
        return getCmdStrFromData(fileName.name().getBytes());
    }

    @Override
    public String fillResponse(int len, byte[] dr, final GloryDE50ResponseWithData response) {
        String err = super.fillResponse(len, dr, response);
        if (err != null) {
            return err;
        }
        byte[] data = response.getData();
        if (data == null) {
            return "Data is null";
        }
        if (data.length != 8) {
            return String.format("Invalid command (%s) response length %d expected 8 bytes hex number", getDescription(), len);
        }
        byte[] b = data;
        int l = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0x30 && b[i] <= 0x3F) {
                l += getHexDigit(b[i]) * Math.pow(16, b.length - i - 1);
            } else {
                return String.format("Invalid digit %d == 0x%x", b[i], b[i]);
            }
        }
        response.setFileSize(l);
        return null;
    }

}
