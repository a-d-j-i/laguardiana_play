package devices.glory.command;


/*
 * Upload a file from DE to TM. If DE have file with same name, DE delete it.
 * NAK is returned when file access is failure. Refer to the Appendix 2 for the
 * file name.
 */
public class StartUpload extends CommandWithFileSizeResponse {

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

    public StartUpload(Files fileName) {
        super((byte) 0x52, "StartUpload");
        setCmdData(fileName.name().getBytes());
    }
}
