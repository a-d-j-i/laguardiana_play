package devices.glory.command;


/* 
 * Update a software on DE.
 * Set the file name by 8.3 format.
 * If the file is not found, NAK is returned.
 * During updating software, DE cannot return response.
 */
public class ProgramUpdate extends CommandWithAckResponse {

    public ProgramUpdate(String fileName) {
        super((byte) 0x50, "ProgramUpdate");
        setCmdData(fileName.getBytes());
    }
}
