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
<<<<<<< HEAD:play/app/controllers/PrinterController.java
            o[0] = ModelFacade.getPrinterPort();
            o[1] = ModelFacade.getPrinterState();
            renderJSON(o);
        }
        renderArgs.put("printers", ModelFacade.getPrinters());
        renderArgs.put("printerStatus", ModelFacade.getPrinterState());
        renderArgs.put("currentPrinter", ModelFacade.getPrinterPort());

=======
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
>>>>>>> 5b6aebaccd5ff8e589943295d3e6f39d9c74b253:app/controllers/PrinterController.java
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
            ModelFacade.printerTest(printer, "PrinterController/test.html", renderArgs.data, 77, 30);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        listPrinters(null);
    }
}
