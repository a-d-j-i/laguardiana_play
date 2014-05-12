 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.ioboard.IoBoard;
import devices.printer.OSPrinter;
import models.actions.UserAction.StateApi;

/**
 *
 * @author adji
 */
public class PrinterNotReady extends ActionState {

    final protected ActionState prevState;

    public PrinterNotReady(StateApi stateApi, ActionState prevState) {
        super(stateApi);
        this.prevState = prevState;
    }

    @Override
    public String name() {
        return "PRINTER_NOT_READY";
    }
/*
    @Override
    public void onGloryEvent(ManagerStatus m) {
        prevState.onGloryEvent(m);
    }
*/
    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        prevState.onIoBoardEvent(status);
    }

    @Override
    public void onPrinterEvent(OSPrinter.PrinterStatus status) {
        switch (status.getPrinterState()) {
            case PRINTER_READY:
                stateApi.setState(prevState);
                break;
            default:
                super.onPrinterEvent(status);
                break;
        }
    }
}
