/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.ioboard;

/**
 *
 * @author adji
 */
public class IoBoardError {

    public enum ERROR_CODE {

        IOBOARD_FW_ERROR, IOBOARD_COMMUNICATION_TIMEOUT, IOBOARD_COMMUNICATION_ERROR;
    }
    final ERROR_CODE errorCode;
    final String detail;

    public IoBoardError(ERROR_CODE errorCode, String detail) {
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ERROR_CODE getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "AppError{" + "error=" + errorCode + ", detail=" + detail + '}';
    }
}
