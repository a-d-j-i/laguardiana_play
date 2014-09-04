package models;

import devices.printer.PrinterError;
import play.Logger;

/**
 *
 * @author adji
 */
public class ModelError {

    public enum ERROR_CODE {

        APPLICATION_ERROR, BAG_NOT_INPLACE, SHUTTER_NOT_CLOSED, SHUTTER_NOT_OPENING, ERROR_TRYING_TO_COLLECT, ESCROW_JAMED, FAILED_TO_START_ACTION;
    }
//    private GloryDE50DeviceErrorEvent gloryError = null;
    private PrinterError printerError = null;
    private ERROR_CODE errorCode;
    private String detail;

    synchronized public boolean isError() {
        return errorCode != null || printerError != null;
    }
    /*
     synchronized public void setError(GloryDE50DeviceErrorEvent error) {
     Logger.debug("Error in gloryError %s", error);
     this.gloryError = error;
     }
     */

    synchronized public void setError(PrinterError error) {
        Logger.debug("Error in printerError %s", error);
        this.printerError = error;
    }

    synchronized public void setError(ERROR_CODE errorCode, String detail) {
        Logger.debug("Error in AppError %s %s", errorCode, detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
    /*
     synchronized public GloryDE50DeviceErrorEvent getGloryError() {
     return gloryError;
     }
     */

    synchronized public PrinterError getPrinterError() {
        return printerError;
    }

    synchronized public ERROR_CODE getErrorCode() {
        return errorCode;
    }

    synchronized public String getDetail() {
        return detail;
    }

    /*
     synchronized void clearGloryError() {
     Logger.debug("--> Model glory error cleared");
     this.gloryError = null;
     }

     synchronized void clearPrinterError() {
     Logger.debug("--> Model printer error cleared");
     this.gloryError = null;
     }
     */
    synchronized void clearErrorCodeError() {
        Logger.debug("--> Model errorCode error cleared");
        this.errorCode = null;
        this.detail = null;
    }

    @Override
    public String toString() {
        return "ModelError{" + "printerError=" + printerError + ", errorCode=" + errorCode + ", detail=" + detail + '}';
    }

}
