package controllers;

import devices.printer.Printer;
import java.util.List;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.ModelFacade;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class PrinterController extends Controller {

    public static void listPrinters(String printer) {
        if (printer != null) {
            Logger.debug("Changing printer to %s", printer);
            ModelFacade.setCurrentPrinter(printer);
        }
        Printer p = ModelFacade.getCurrentPrinter();
        if (request.isAjax()) {
            Object o[] = new Object[2];
            if (p != null) {
                o[0] = p.getPort();
                o[1] = p.getInternalState().toString();
            }
            renderJSON(o);
        }
        renderArgs.put("printers", ModelFacade.getPrinters());
        if (p != null) {
            renderArgs.put("printerStatus", p.getInternalState());
            renderArgs.put("currentPrinter", p.getPort());
        }

        render();
    }

    public static void billDeposit() {
        List<BillDeposit> depositList = BillDeposit.findAll();
        BillDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print(true);
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void envelopeDeposit_finish() {
        List<EnvelopeDeposit> depositList = EnvelopeDeposit.findAll();
        EnvelopeDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print(true);
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void envelopeDeposit_start() {
        List<EnvelopeDeposit> depositList = EnvelopeDeposit.findAll();
        EnvelopeDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.printStart();
        d.setRenderArgs(renderArgs.data);
        render();
    }

    public static void test(String printer) {
        try {
            ModelFacade.print(printer, "PrinterController/test.html", renderArgs.data, 77, 30, false, 1);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        listPrinters(null);
    }
}
