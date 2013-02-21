/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printer;

/**
 *
 * @author adji
 */
public class PrinterStatus {

    public enum ERROR_CODE {

        PRINTING_DONE, TEMPLATE_NOT_FOUND, IO_EXCEPTION, PRINTER_NOT_FOUND;
    }
    final ERROR_CODE errorCode;
    final String detail;

    public PrinterStatus(ERROR_CODE errorCode, String detail) {
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public boolean isError() {
        return errorCode != ERROR_CODE.PRINTING_DONE;
    }

    public ERROR_CODE getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "PrinterStatus{" + "error=" + errorCode + ", detail=" + detail + '}';
    }
}
