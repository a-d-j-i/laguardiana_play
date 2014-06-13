package models.facade.state.substate;

import devices.ioboard.IoBoard;
import devices.printer.OSPrinter;

/**
 *
 * @author adji
 */
public class PrinterNotReady extends ModelFacadeSubStateAbstract {

    final protected ModelFacadeSubStateAbstract prevState;

    public PrinterNotReady(ModelFacadeSubStateAbstract prevState, ModelFacadeSubStateApi api) {
        super(api);
        this.prevState = prevState;
    }

    @Override
    public String getSubStateName() {
        return "PRINTER_NOT_READY";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     prevState.onGloryEvent(m);
     }

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
     */
}
