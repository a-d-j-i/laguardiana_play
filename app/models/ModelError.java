/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.glory.manager.GloryManagerError;
import devices.ioboard.IoBoardError;
import devices.printer.PrinterError;
import play.Logger;

/**
 *
 * @author adji
 */
public class ModelError {

    public enum ERROR_CODE {

        APPLICATION_ERROR, BAG_NOT_INPLACE, SHUTTER_NOT_CLOSED, SHUTTER_NOT_OPENING, ERROR_TRYING_TO_COLLECT, ESCROW_JAMED;
    }
    private GloryManagerError gloryError = null;
    private PrinterError printerError = null;
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

    synchronized public void setError(PrinterError error) {
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

    synchronized public PrinterError getPrinterError() {
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

    synchronized void clearIoBoardError() {
        Logger.debug("--> Model ioboard error cleared");
        this.ioBoardError = null;
    }

    synchronized void clearGloryError() {
        Logger.debug("--> Model glory error cleared");
        this.gloryError = null;
    }

    synchronized void clearPrinterError() {
        Logger.debug("--> Model printer error cleared");
        this.gloryError = null;
    }

    synchronized void clearErrorCodeError() {
        Logger.debug("--> Model errorCode error cleared");
        this.errorCode = null;
        this.detail = null;
    }

    @Override
    public String toString() {
        return "gloryError = { " + gloryError + " }, printerError = { " + printerError + " }, ioBoardError = { " + ioBoardError + "}, errorCode = { " + errorCode + " }, detail = { " + detail + " }";
    }
}
