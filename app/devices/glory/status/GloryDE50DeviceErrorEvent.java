/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.status;

/**
 *
 * @author adji
 */
public class GloryDE50DeviceErrorEvent {

    public enum ERROR_CODE {

        GLORY_MANAGER_ERROR, STORING_ERROR_CALL_ADMIN, BILLS_IN_ESCROW_CALL_ADMIN, CASSETE_FULL;
    }
    final ERROR_CODE errorCode;
    final String detail;

    public GloryDE50DeviceErrorEvent(ERROR_CODE errorCode, String detail) {
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
        return "code = " + errorCode + ", detail = " + detail;
    }
}
