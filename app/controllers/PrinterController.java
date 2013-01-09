package controllers;

import devices.DeviceFactory;
import java.util.List;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgBag;
import models.db.LgZ;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class PrinterController extends Controller {
    
    public static void listPrinters() {
        renderArgs.put("printers", DeviceFactory.getPrinter().printers.values());
        render();
    }
    
    public static void billDeposit() {
        List<BillDeposit> depositList = BillDeposit.findAll();
        BillDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print();
        d.setRenderArgs(renderArgs.data);
        render();
    }
    
    public static void currentBagTotals() {
        LgBag b = LgBag.getCurrentBag();
        b.print();
        b.setRenderArgs(renderArgs.data);
        render();
    }
    
    public static void envelopeDeposit_finish() {
        List<EnvelopeDeposit> depositList = EnvelopeDeposit.findAll();
        EnvelopeDeposit d = depositList.get(0);
        //BillDeposit d = BillDeposit.findById(33);
        d.print();
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
    
    public static void currentZTotals() {
        LgZ z = LgZ.getCurrentZ();
        z.print();
        z.setRenderArgs(renderArgs.data);
        render();
    }
    
    public static void test() {
        try {
            //DeviceFactory.getPrinter().printAttributes();
            DeviceFactory.getPrinter().print("PrinterController/test.html", renderArgs.data, 150);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        render();
    }
}
