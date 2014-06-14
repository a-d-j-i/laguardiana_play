package models;

import models.facade.status.ModelFacadeStateStatus;
import models.facade.FacadeJob;
import controllers.BillDepositController;
import controllers.CountController;
import controllers.EnvelopeDepositController;
import controllers.FilterController;
import devices.device.DeviceInterface;
import devices.printer.OSPrinter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.print.PrintService;
import machines.Machine;
import machines.MachineP500_GLORY;
import machines.MachineP500_MEI;
import machines.events.MachineEvent;
import machines.events.MachineEventListener;
import models.db.LgBag;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgDeviceSlot;
import models.facade.state.ModelFacadeStateAbstract;
import models.facade.state.ModelFacadeStateDepositBill;
import models.facade.state.ModelFacadeStateCount;
import models.facade.state.ModelFacadeStateDepositEnvelope;
import models.facade.state.ModelFacadeStateFilter;
import models.facade.state.ModelFacadeStateWaiting;
import play.Logger;

/**
 * @author adji
 */
public class ModelFacade {

    private enum MachineType {

        P500() {
                    @Override
                    Machine getMachineInstance() {
                        return new MachineP500_GLORY();
                    }
                },
        P500_MEI {

                    @Override
                    Machine getMachineInstance() {
                        return new MachineP500_MEI();
                    }
                },;

        @Override
        public String toString() {
            return name();
        }

        abstract Machine getMachineInstance();

        static public MachineType getMachineType(String machineType) throws IllegalArgumentException {
            return MachineType.valueOf(machineType.toUpperCase());
        }
    };

    static public class ModelFacadeStateApi {

        final private Machine machine;
        private ModelFacadeStateAbstract currentState = new ModelFacadeStateWaiting(this);

        public ModelFacadeStateApi() {
            MachineType machineType = MachineType.getMachineType(Configuration.getMachineType());
            machine = machineType.getMachineInstance();
            machine.addEventListener(new MachineEventListener() {
                public void onMachineEvent(final MachineEvent evt) {
                    new FacadeJob<Boolean>() {

                        @Override
                        public void doJob() {
                            currentState.onMachineEvent(evt);
                        }
                    }.now();
                }
            });
        }

        public Machine getMachine() {
            return machine;
        }

        // called by inner thread.
        public boolean setCurrentState(ModelFacadeStateAbstract state) {
            // give it an oportunity to initialize itself.
            currentState = state.init();
            return currentState == state;
        }

        private ModelFacadeStateStatus getStatus() {
            return new FacadeJob<ModelFacadeStateStatus>() {

                @Override
                public ModelFacadeStateStatus doJobWithResult() {
                    return api.currentState.getStatus();
                }
            }.runNow();
        }

    }
    final private static ModelFacadeStateApi api = new ModelFacadeStateApi();

    public static void start() throws Exception {
        LgDeposit.closeUnfinished();
        Configuration.initCrapId();
        api.getMachine().start();
    }

    public static void stop() {
        api.getMachine().stop();
    }

    public static boolean startBillDepositAction(BillDepositController.BillDepositData data) {
        // Can be put into machine, but till now every machine has it own bag.
        if (!Configuration.isIgnoreBag() && !isBagReady(false)) {
            Logger.info("Can't start bag not ready");
            return false;
        }
        ModelFacadeStateDepositBill depositAction = new ModelFacadeStateDepositBill(api, data);
        return startAction(depositAction);
    }

    public static boolean startCountingAction(CountController.CountData data) {
        ModelFacadeStateCount countingAction = new ModelFacadeStateCount(api, data);
        return startAction(countingAction);
    }

    public static boolean startFilterAction(FilterController.FilterData data) {
        ModelFacadeStateFilter filteringAction = new ModelFacadeStateFilter(api, data);
        return startAction(filteringAction);
    }

    public static boolean startEnvelopeDepositAction(EnvelopeDepositController.EvenlopeDepositData data) {
        // Can be put into machine, but till now every machine has it own bag.
        if (!Configuration.isIgnoreBag() && !isBagReady(false)) {
            Logger.info("Can't start bag not ready");
            return false;
        }
        ModelFacadeStateDepositEnvelope envelopeDepositAction = new ModelFacadeStateDepositEnvelope(api, data);
        return startAction(envelopeDepositAction);
    }

    static private boolean startAction(final ModelFacadeStateAbstract userAction) {
        return new FacadeJob<Boolean>() {

            @Override
            public Boolean doJobWithResult() {
                return api.currentState.startAction(userAction);
            }
        }.runNow();
    }

    public static boolean finishAction() {
        return new FacadeJob<Boolean>() {

            @Override
            public Boolean doJobWithResult() {
                return api.currentState.finish();
            }
        }.runNow();
    }

    static public ModelFacadeStateStatus getStateStatus() {
        return api.getStatus();
    }

    public static boolean cancel() {
        return new FacadeJob<Boolean>() {

            @Override
            public Boolean doJobWithResult() {
                return api.currentState.cancel();
            }
        }.runNow();
    }

    public static boolean accept() {
        return new FacadeJob<Boolean>() {

            @Override
            public Boolean doJobWithResult() {
                return api.currentState.accept();
            }
        }.runNow();
    }

    public static boolean canceTimeout() {
        return new FacadeJob<Boolean>() {

            @Override
            public Boolean doJobWithResult() {
                return api.currentState.suspendTimeout();
            }
        }.runNow();
    }

    public static boolean isBagReady(boolean envelope) {
        if (Configuration.isIgnoreBag()) {
            return true;
        }
        if (!isBagInplace()) {
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
        Logger.debug("isBagReady quantity : %s", iq);
        if (Configuration.isBagFull(iq.bills, iq.envelopes)) {
            Logger.info("Can't start bag full");
            //modelError.setError(ModelError.ERROR_CODE.BAG_FULL, "Bag full too many bills and evenlopes");
            return false;
        }
        return true;
    }

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
////        });

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

    public static DeviceInterface findDeviceById(Integer deviceId) {
        return api.getMachine().findDeviceById(deviceId);
    }

    public static boolean isBagInplace() {
        return api.getMachine().isBagInplace();
    }

    public static Object getDevices() {
        return api.getMachine().getDevices();
    }

    private interface BillListVisitor {

        public void visit(LgBillType billType, Integer desired, Integer current);
    }

    public static Collection<BillQuantity> getBillQuantities(int currencyId) {
        final SortedMap<BillValue, BillQuantity> ret = new TreeMap<BillValue, BillQuantity>();
        visitBillList(currencyId, new BillListVisitor() {
            public void visit(LgBillType billType, Integer desired, Integer current) {
                BillValue bv = billType.getValue();
                BillQuantity billQuantity = ret.get(bv);
                if (billQuantity == null) {
                    billQuantity = new BillQuantity(bv);
                }
                billQuantity.quantity += current;
                billQuantity.desiredQuantity += desired;
                ret.put(bv, billQuantity);

            }
        });
        return ret.values();
    }

    private static void visitBillList(int currencyId, BillListVisitor visitor) {
        List<LgBillType> billTypes = LgBillType.find(currencyId);
        Map<LgDeviceSlot, Integer> desiredQuantity = api.getMachine().getCurrentQuantity();
        Map<LgDeviceSlot, Integer> currentQuantity = api.getMachine().getDesiredQuantity();

        // Sum over all bill types ignoring the device and slot they came from.
        // Plan B: separate the slots by device.
        for (LgBillType billType : billTypes) {
            Integer desired = 0;
            Integer current = 0;
            for (LgDeviceSlot s : billType.slots) {
                if (currentQuantity.containsKey(s)) {
                    current = currentQuantity.get(s);
                }
                if (desiredQuantity.containsKey(s)) {
                    desired = desiredQuantity.get(s);
                }
                visitor.visit(billType, desired, current);
            }
        }
    }
}
