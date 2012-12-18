package controllers;

import devices.DeviceFactory;
import java.util.List;
import models.Bill;
import models.BillDeposit;
import models.Configuration;
import models.lov.Currency;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class PrinterController extends Controller {

    static final boolean TO_PRINTER = true;

    public static void listPrinters() {
        renderArgs.put("printers", DeviceFactory.getPrinter().printers.values());
        render();
    }

    public static void billDeposit() {
        BillDepositController.FormData formData = new BillDepositController.FormData();
        formData.currency.currency = new Currency();
        formData.currency.currency.textId = "Pesos";
        renderArgs.put("formData", formData);
        renderArgs.put("clientCode", Configuration.getClientDescription());
        /*List<BillDeposit> depositList = BillDeposit.findAll();
         BillDeposit deposit = depositList.get(0);*/
        BillDeposit deposit = BillDeposit.findById(33);
        List<Bill> bl = deposit.getBillList();
        renderArgs.put("billData", bl);
        renderArgs.put("depositTotal", deposit.getTotal());
        renderArgs.put("deposit", deposit);
        renderArgs.put("envelopes", deposit.envelopes);
        print();
        render();
    }

    public static void currentBagTotals() {
        renderArgs.put("clientCode", Configuration.getClientDescription());

        /*List<BillDeposit> depositList = BillDeposit.findAll();
         BillDeposit deposit = depositList.get(0);*/
        BillDeposit deposit = BillDeposit.findById(33);
        List<Bill> bl = deposit.getBillList();
        renderArgs.put("billData", bl);
        renderArgs.put("depositTotal", deposit.getTotal());
        renderArgs.put("deposit", deposit);
        renderArgs.put("envelopes", deposit.envelopes);
        print();
        render();
    }

    public static void envelopeDeposit_finish() {
        EnvelopeDepositController.FormData formData = new EnvelopeDepositController.FormData();
        renderArgs.put("formData", formData);

        renderArgs.put("clientCode", Configuration.getClientDescription());

        /*List<BillDeposit> depositList = BillDeposit.findAll();
         BillDeposit deposit = depositList.get(0);*/
        BillDeposit deposit = BillDeposit.findById(33);
        List<Bill> bl = deposit.getBillList();
        renderArgs.put("billData", bl);
        renderArgs.put("depositTotal", deposit.getTotal());
        renderArgs.put("deposit", deposit);
        renderArgs.put("envelopes", deposit.envelopes);
        print();
        render();
    }

    public static void envelopeDeposit_start() {
        EnvelopeDepositController.FormData formData = new EnvelopeDepositController.FormData();
        renderArgs.put("formData", formData);

        renderArgs.put("clientCode", Configuration.getClientDescription());

        /*List<BillDeposit> depositList = BillDeposit.findAll();
         BillDeposit deposit = depositList.get(0);*/
        BillDeposit deposit = BillDeposit.findById(33);
        List<Bill> bl = deposit.getBillList();
        renderArgs.put("billData", bl);
        renderArgs.put("depositTotal", deposit.getTotal());
        renderArgs.put("deposit", deposit);
        renderArgs.put("envelopes", deposit.envelopes);
        print();
        render();
    }

    public static void currentZTotals() {
        BillDepositController.FormData formData = new BillDepositController.FormData();
        formData.currency.currency = new Currency();
        formData.currency.currency.textId = "Pesos";
        renderArgs.put("formData", formData);
        renderArgs.put("clientCode", Configuration.getClientDescription());
        /*List<BillDeposit> depositList = BillDeposit.findAll();
         BillDeposit deposit = depositList.get(0);*/
        BillDeposit deposit = BillDeposit.findById(33);
        List<Bill> bl = deposit.getBillList();
        renderArgs.put("billData", bl);
        renderArgs.put("depositTotal", deposit.getTotal());
        renderArgs.put("deposit", deposit);
        renderArgs.put("envelopes", deposit.envelopes);
        print();
        render();
    }

    public static void test() {
        print();
        render();
    }

    @Util
    static void print() {
        if (TO_PRINTER) {
            try {
                //DeviceFactory.getPrinter().printAttributes();
                DeviceFactory.getPrinter().print("billDeposit", renderArgs.data, 120);
            } catch (Throwable ex) {
                Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
            }
        }
    }
}
