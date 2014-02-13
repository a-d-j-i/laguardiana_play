package controllers;

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
        if (request.isAjax()) {
            Object o[] = new Object[2];
            o[0] = ModelFacade.getCurrentPrinter().getPort();
            o[1] = ModelFacade.getCurrentPrinter().getInternalState().toString();
            renderJSON(o);
        }
        renderArgs.put("printers", ModelFacade.getPrinters());
        renderArgs.put("printerStatus", ModelFacade.getCurrentPrinter().getInternalState());
        renderArgs.put("currentPrinter", ModelFacade.getCurrentPrinter().getPort());

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
            ModelFacade.print(printer, "PrinterController/test.html", renderArgs.data, 77, 30);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        listPrinters(null);
    }
}
