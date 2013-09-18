/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printer;

/**
 *
 * @author adji
 */
public class PrinterError {

    public enum ERROR_CODE {

        IO_EXCEPTION,
        TEMPLATE_NOT_FOUND,
        PRINTER_NOT_FOUND;
    }
    final ERROR_CODE errorCode;
    final String detail;

    public PrinterError(ERROR_CODE errorCode, String detail) {
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
        return "PrinterError{" + "errorCode=" + errorCode + ", detail=" + detail + '}';
    }
}
