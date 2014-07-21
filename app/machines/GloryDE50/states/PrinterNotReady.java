package machines.GloryDE50.states;

import devices.device.status.DeviceStatusInterface;
import machines.states.MachineStateAbstract;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class PrinterNotReady extends MachineStateAbstract {

    final protected MachineStateAbstract prevState;

    public PrinterNotReady(MachineStateAbstract prevState, MachineStateApiInterface machine) {
        super(machine);
        this.prevState = prevState;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MachineStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//
//    @Override
//    public String getStateName() {
//        return "PRINTER_NOT_READY";
//    }
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
