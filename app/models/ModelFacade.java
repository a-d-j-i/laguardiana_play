package models;

import controllers.CountController;
import controllers.FilterController;
import devices.printer.OSPrinter;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.MachineInterface;
import machines.P500_MEI.MachineP500_MEI;
import machines.jobs.MachineJobAcceptDeposit;
import machines.jobs.MachineJobCancelDeposit;
import machines.jobs.MachineJobConfirmDeposit;
import machines.jobs.MachineJobGetCurrentStatus;
import machines.jobs.MachineJobIsBagFull;
import machines.jobs.MachineJobIsBagReady;
import machines.jobs.MachineJobStartBillDepositAction;
import machines.jobs.MachineJobStartCountingAction;
import machines.jobs.MachineJobStartEnvelopeDepositAction;
import machines.jobs.MachineJobStartFilterAction;
import machines.status.MachineStatus;
import models.db.LgUser;
import models.lov.Currency;
import play.Logger;

/**
 * @author adji
 */
public class ModelFacade {

    private enum MachineType {

        P500() {
                    @Override
                    MachineAbstract getMachineInstance() {
                        return new MachineP500_MEI();
                    }
                },
        P500_MEI {

                    @Override
                    MachineAbstract getMachineInstance() {
                        return new MachineP500_MEI();
                    }
                },
        P500_MEI_X2 {

                    @Override
                    MachineAbstract getMachineInstance() {
                        return new MachineP500_MEI();
                    }
                },
        P500_MEI_GLORY {

                    @Override
                    MachineAbstract getMachineInstance() {
                        return new MachineP500_MEI();
                    }
                },;

        @Override
        public String toString() {
            return name();
        }

        abstract MachineInterface getMachineInstance();

        static public MachineType getMachineType(String machineType) throws IllegalArgumentException {
            return MachineType.valueOf(machineType.toUpperCase());
        }
    };

    static private MachineInterface machine = null;

    synchronized public static void start() throws Exception {
        Configuration.initCrapId();
        MachineType machineType = MachineType.getMachineType(Configuration.getMachineType());
        machine = machineType.getMachineInstance();
        Logger.debug("Executing machine start job");
        machine.start();
    }

    synchronized public static void stop() {
        if (machine != null) {
            machine.stop();
        }
    }

    static public boolean startBillDepositAction(LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        return machine.execute(new MachineJobStartBillDepositAction(machine, user, currency, userCode, userCodeLovId)
        );
    }

    static public boolean startCountingAction(CountController.CountData data) {
        return machine.execute(new MachineJobStartCountingAction(machine, data));
    }

    static public boolean startFilterAction(FilterController.FilterData data) {
        return machine.execute(new MachineJobStartFilterAction(machine, data));
    }

    static public boolean startEnvelopeDepositAction(LgUser user, String userCode, Integer userCodeLovId) {
        return machine.execute(new MachineJobStartEnvelopeDepositAction(machine, user, userCode, userCodeLovId));
    }

    static public boolean accept() {
        return machine.execute(new MachineJobAcceptDeposit(machine));
    }

    static public boolean cancel() {
        return machine.execute(new MachineJobCancelDeposit(machine));
    }

    static public boolean confirmAction() {
        return machine.execute(new MachineJobConfirmDeposit(machine));
    }

    static public boolean canceTimeout() {
        return false;
    }

    static public MachineStatus getCurrentStatus() {
        return machine.execute(new MachineJobGetCurrentStatus(machine));
    }

    static public boolean isBagFull(final boolean envelope) {
        return machine.execute(new MachineJobIsBagReady(machine));
    }

    public static boolean isBagReady() {
        return machine.execute(new MachineJobIsBagFull(machine));
    }

    // TODO: Consider adding this to machine !!!.
    public static void setCurrentPrinter(String prt) {
        if (prt == null) {
            prt = Configuration.getDefaultPrinter();
            if (prt == null) {
                Logger.error("Default printer must be configured");
                return;
            }
        }

        PrintService p = (PrintService) OSPrinter.printers.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            return;
        }
        Configuration.setDefaultPrinter(prt);
////        Printer currPrinter = DeviceFactory.getPrinter(prt);
////        currPrinter.addObserver(new Observer() {
////            public void update(Observable o, Object data) {
////                Promise now = new OnPrinterEvent((Printer.PrinterStatus) data).now();
////            }
////        });Map<LgDeviceSlot, Integer> 

////        Printer oldPrinter = printer.getAndSet(currPrinter);
////        if (oldPrinter != null) {
        //oldPrinter.close();
////        }
    }

    public static boolean isReadyToPrint() {
////        return printer.get().needCheck();
        return false;
    }

    public static void print(String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        print(null, templateName, args, paperWidth, paperLen);
    }

    public static void print(String prt, String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        if (prt == null) {
            prt = Configuration.getDefaultPrinter();
            if (prt == null) {
                Logger.error("Default printer must be configured");
                return;
            }
        }

        PrintService p = (PrintService) OSPrinter.printers.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            //return;
        }
////        Printer pp = DeviceFactory.getPrinter(prt);
////        pp.print(templateName, args, paperWidth, paperLen);
////        if (pp != printer.get()) {
        //pp.close();
////        }
    }
    /*
     public static Object getPrinters() {
     return OSPrinter.printers.values();
     }
     */

    public static OSPrinter getCurrentPrinter() {
////        return printer.get();
        return null;
    }

    public static MachineDeviceDecorator findDeviceById(Integer deviceId) {
        return machine.findDeviceById(deviceId);
    }

    public static List<MachineDeviceDecorator> getDevices() {
        return machine.getDevices();
    }
}
