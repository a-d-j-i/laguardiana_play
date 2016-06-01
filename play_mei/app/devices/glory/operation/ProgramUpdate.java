package devices.glory.operation;


/* 
 * Update a software on DE.
 * Set the file name by 8.3 format.
 * If the file is not found, NAK is returned.
 * During updating software, DE cannot return response.
 */
public class ProgramUpdate extends OperationWithAckResponse {

    final String fileName;

    public ProgramUpdate(String fileName) {
        super(0x50);
        this.fileName = fileName;
    }

    @Override
    public byte[] getCmdStr() {
        return getCmdStrFromData(fileName.getBytes());
    }
}
