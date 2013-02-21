/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.glory.manager.GloryManagerError;
import devices.ioboard.IoBoardError;
import play.Logger;

/**
 *
 * @author adji
 */
public class ModelError {

    public enum ERROR_CODE {

        APP_ERROR, BAG_NOT_INPLACE, SHUTTER_NOT_OPEN;
    }
    private GloryManagerError gloryError = null;
    private IoBoardError ioBoardError = null;
    private ERROR_CODE errorCode;
    private String detail;

    synchronized public boolean isError() {
        return errorCode != null || gloryError != null || ioBoardError != null;
    }

    synchronized public void setError(GloryManagerError error) {
        Logger.debug("Error in gloryError %s", error);
        this.gloryError = error;
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

    synchronized void clearError() {
        this.errorCode = null;
        this.detail = null;
        this.gloryError = null;
        this.ioBoardError = null;
    }

    @Override
    synchronized public String toString() {
        return "ModelError{" + "gloryError=" + gloryError + ", ioBoardError=" + ioBoardError + ", errorCode=" + errorCode + ", detail=" + detail + '}';
    }
}
