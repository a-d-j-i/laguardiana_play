package devices.glory.operation;

/*   
 * Set the file name by 8.3 format, or name in the Appendix 2.
 * NAK is returned when file access is failure.
 */
public class GetFileInfo extends OperationdWithDataResponse {

    GetFileInfo(String fileName) {
        super(0x54);
        throw new IllegalArgumentException("Must be implemented GetFileInfo");
    }
}
