package controllers;

import devices.DeviceFactory;
import java.util.List;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.ModelFacade;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class PrinterController extends Controller {

    public static void listPrinters() {
        renderArgs.put("printers", DeviceFactory.getPrinter().printers.values());
        renderArgs.put("printerStatus", ModelFacade.getPrinterStatus());
        renderArgs.put("currentPrinter", ModelFacade.getCurrentPrinter());

        render();
    }

    public static void billDeposit() {
        List<BillDeposit> depositList = BillDeposit.findAll();
        BillDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print(DeviceFactory.getPrinter(), true);
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void envelopeDeposit_finish() {
        List<EnvelopeDeposit> depositList = EnvelopeDeposit.findAll();
        EnvelopeDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print(DeviceFactory.getPrinter(), true);
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void envelopeDeposit_start() {
        List<EnvelopeDeposit> depositList = EnvelopeDeposit.findAll();
        EnvelopeDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.printStart(DeviceFactory.getPrinter());
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void test() {
        try {
            //DeviceFactory.getPrinter().printAttributes();
            DeviceFactory.getPrinter().print("PrinterController/test.html", renderArgs.data, 80, 30);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        MenuController.hardwareMenu(null);
    }
}
