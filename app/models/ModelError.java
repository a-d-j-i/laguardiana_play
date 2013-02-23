/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.glory.manager.GloryManagerError;
import devices.ioboard.IoBoardError;
import devices.printer.PrinterStatus;
import play.Logger;

/**
 *
 * @author adji
 */
public class ModelError {

    public enum ERROR_CODE {

        APP_ERROR, BAG_NOT_INPLACE, SHUTTER_NOT_OPEN, ERROR_TRYING_TO_COLLECT, BAG_FULL;
    }
    private GloryManagerError gloryError = null;
    private PrinterStatus printerError = null;
    private IoBoardError ioBoardError = null;
    private ERROR_CODE errorCode;
    private String detail;

    synchronized public boolean isError() {
        return errorCode != null || gloryError != null || ioBoardError != null || printerError != null;
    }

    synchronized public void setError(GloryManagerError error) {
        Logger.debug("Error in gloryError %s", error);
        this.gloryError = error;
    }

    synchronized public void setError(PrinterStatus error) {
        Logger.debug("Error in printerError %s", error);
        this.printerError = error;
    }

    synchronized public void setError(IoBoardError error) {
        Logger.debug("Error in ioBoardError %s", error);
        this.ioBoardError = error;
    }

    synchronized public void setError(ERROR_CODE errorCode, String detail) {
        Logger.debug("Error in AppError %s %s", errorCode, detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    synchronized public GloryManagerError getGloryError() {
        return gloryError;
    }

    synchronized public PrinterStatus getPrinterError() {
        return printerError;
    }

    synchronized public IoBoardError getIoBoardError() {
        return ioBoardError;
    }

    synchronized public ERROR_CODE getErrorCode() {
        return errorCode;
    }

    synchronized public String getDetail() {
        return detail;
    }

    synchronized void clearError() {
        Logger.debug("--> Model error cleared");
        this.errorCode = null;
        this.detail = null;
        this.gloryError = null;
        this.printerError = null;
        this.ioBoardError = null;
    }

    @Override
    public String toString() {
        return "gloryError = { " + gloryError + " }, printerError = { " + printerError + " }, ioBoardError = { " + ioBoardError + "}, errorCode = { " + errorCode + " }, detail = { " + detail + " }";
    }
}
