package models;

import controllers.CountController;
import controllers.FilterController;
import controllers.Secure;
import devices.printer.Printer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import machines.P500_GloryDE50.MachineP500_GLORY;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.MachineInterface;
import machines.MachinePrinterDecorator;
import machines.P500_MEI.MachineP500_MEI;
import machines.jobs.MachineJobAcceptDeposit;
import machines.jobs.MachineJobCancelDeposit;
import machines.jobs.MachineJobConfirmDeposit;
import machines.jobs.MachineJobGetCurrentStatus;
import machines.jobs.MachineJobIsBagReady;
import machines.jobs.MachineJobReset;
import machines.jobs.MachineJobStartBillDepositAction;
import machines.jobs.MachineJobStartCountingAction;
import machines.jobs.MachineJobStartEnvelopeDepositAction;
import machines.jobs.MachineJobStartFilterAction;
import machines.jobs.MachineJobStoringErrorReset;
import machines.status.MachineStatus;
import models.db.LgBag;
import models.db.LgUser;
import play.Logger;
import play.templates.Template;
import play.templates.TemplateLoader;

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
        P500_GLORY {

                    @Override
                    MachineAbstract getMachineInstance() {
                        return new MachineP500_GLORY();
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
    final static private MachinePrinterDecorator printer = new MachinePrinterDecorator();

    synchronized public static void start() throws Exception {
        MachineType machineType = MachineType.getMachineType(Configuration.getMachineType());
        machine = machineType.getMachineInstance();
        Logger.debug("Executing machine start job");
        machine.start();
        printer.start();
    }

    synchronized public static void stop() {
        if (machine != null) {
            machine.stop();
        }
        if (printer != null) {
            printer.stop();
        }
    }

    public static Object getLockedByUser() {
        MachineStatus status = getCurrentStatus();
        if (status == null || status.getCurrentUserId() == null) {
            return false;
        }
        LgUser currentUser = LgUser.findById(status.getCurrentUserId());
        if (currentUser == null) {
            return false;
        }
        if (!Secure.isLocked(status.getCurrentUserId())) {
            return null;
        }
        return currentUser.username;
    }

    static public boolean startBillDepositAction(BillDeposit refDeposit) {
        return machine.execute(new MachineJobStartBillDepositAction(machine, refDeposit));
    }

    static public boolean startCountingAction(CountController.CountData data) {
        return machine.execute(new MachineJobStartCountingAction(machine, data));
    }

    static public boolean startFilterAction(FilterController.FilterData data) {
        return machine.execute(new MachineJobStartFilterAction(machine, data));
    }

    static public boolean startEnvelopeDepositAction(EnvelopeDeposit refDeposit) {
        return machine.execute(new MachineJobStartEnvelopeDepositAction(machine, refDeposit));
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

    private static boolean isMachineBagReady() {
        return machine.execute(new MachineJobIsBagReady(machine));
    }

    public static boolean isBagReady(boolean envelope) {
        if (Configuration.isIgnoreBag()) {
            Logger.debug("isBagReady ignore bug");
            return true;
        }
        if (!ModelFacade.isMachineBagReady()) {
            Logger.info("Can't start bag removed");
            return false;
        }
        LgBag currentBag = LgBag.getCurrentBag();
        ItemQuantity iq = currentBag.getItemQuantity();
        // for an envelope deposit I neet at least space for one envelope more.
        if (envelope) {
            iq.envelopes++;
            iq.bills--;
        }
        if (Configuration.isBagFull(iq.bills, iq.envelopes)) {
            Logger.debug("isBagReady quantity : %s", iq);
            Logger.info("Can't start bag full for %s", envelope ? "ENVELOPE" : "BILL");
            //modelError.setError(ModelError.ERROR_CODE.BAG_FULL, "Bag full too many bills and evenlopes");
            return false;
        }
        return true;
    }

    synchronized public static boolean errorReset() {
        return machine.execute(new MachineJobReset(machine));
    }

    synchronized public static boolean storingErrorReset() {
        return machine.execute(new MachineJobStoringErrorReset(machine));
    }

    public static MachineDeviceDecorator findDeviceById(Integer deviceId) {
        return machine.findDeviceById(deviceId);
    }

    public static List<MachineDeviceDecorator> getDevices() {
        return machine.getDevices();
    }

    public static boolean isReadyToPrint() {
        return printer.needCheck();
    }

    public static void setCurrentPrinter(String prt) {
        printer.setCurrentPrinter(prt);
    }

    public static String getPrinterPort() {
        return printer.getPrinterPort();
    }

    public static String getPrinterState() {
        return printer.getPrinterState();
    }

    public static void print(String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        printer.print(templateName, args, paperWidth, paperLen);
    }

    public static void printerTest(String prt, String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        Logger.debug("Printing a test to printer : %s", prt);
        final PrintService p = (PrintService) Printer.PRINTERS.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            return;
        }
        Printer prnt = new Printer(p);
        Template template = TemplateLoader.load(templateName);
        if (template == null) {
            template = TemplateLoader.load(templateName + ".html");
        }
        if (template == null) {
            template = TemplateLoader.load(templateName + ".txt");
        }
        if (template == null) {
            Logger.error("invalid template %s", templateName);
            return;
        }
        args.put("currentDate", new Date());
        final String body = template.render(args);
        prnt.print(Configuration.isPrinterTest(), body, paperWidth, paperLen);
    }

    public static Collection<PrintService> getPrinters() {
        return printer.getPrinters();
    }

    public static String getAppVersion() {
        File f = play.Play.getFile("version.txt");
        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            return new String(data, "UTF-8");
        } catch (StringIndexOutOfBoundsException ex) {
            //Logger.error("Error reading release file : %s", ex.toString());
        } catch (IOException ex) {
            //Logger.error("Error reading release file : %s", ex.toString());
        }
        return "cant get app version from " + f.getAbsolutePath();
    }
}
