/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printer;

import devices.device.status.DeviceStatusInterface;
import devices.printer.OSPrinter.PRINTER_STATE;

/**
 *
 * @author adji
 */
public class PrinterState implements DeviceStatusInterface {

    private PRINTER_STATE printerState = null;
    private String stateDesc;
    private PrinterError error = null;
    /*
     synchronized void setState(PRINTER_STATE state, String stateDesc) {
     //Logger.debug("Printer setState prev : printerSTate %s stateDesc %s", state, stateDesc);
     if (this.printerState != state) {
     this.printerState = state;
     setChanged();
     }
     if ((this.stateDesc == null && stateDesc != null)
     || (this.stateDesc != null && !this.stateDesc.equals(stateDesc))) {
     this.stateDesc = stateDesc;
     setChanged();
     }
     if (hasChanged()) {
     Logger.debug("Printer setState : printerSTate %s stateDesc %s", this.printerState, this.stateDesc);
     notifyObservers(new PrinterStatus(this));
     }
     }

     synchronized void setError(PrinterError error) {
     this.error = error;
     setChanged();
     notifyObservers(new PrinterStatus(this));
     }

     synchronized public void clearError() {
     // Don't overwrite the first error!!!.
     if (this.error != null) {
     this.error = null;
     setChanged();
     notifyObservers(new PrinterStatus(this));
     }
     }
     */

    public Enum getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean is(Enum type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean is(Class type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
