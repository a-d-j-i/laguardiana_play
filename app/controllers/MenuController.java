package controllers;

import java.util.HashMap;
import java.util.Map;
import models.Configuration;
import models.ItemQuantity;
import models.ModelFacade;
import models.db.LgBag;
import play.mvc.*;

@With({Secure.class})
public class MenuController extends Controller {

    public static void mainMenu(String back) {
        LgBag currentBag = LgBag.getCurrentBag();
        ItemQuantity iq = currentBag.getItemQuantity();
        Long bagFreeSpace = Configuration.maxBillsPerBag() - Configuration.equivalentBillQuantity(iq);
        if (bagFreeSpace < 0) {
            bagFreeSpace = (long) 0;
        }
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = ModelFacade.printerNeedCheck();
            o[1] = (!Configuration.isIgnoreBag() && !ModelFacade.isIoBoardOk());
            // I need space for at least one envelope. see ModelFacade->isBagReady too.
            iq.envelopes++;
            o[2] = Configuration.isBagFull(iq);
            renderJSON(o);
        }
        String backAction = "MenuController.mainMenu";
        String[] buttons = {"BillDepositController.start", "CountController.start", "EnvelopeDepositController.start", "FilterController.start"};
        String[] extraButtons = {"MenuController.otherMenu"};
        String[] titles = {"main_menu.cash_deposit", "main_menu.count", "main_menu.envelope_deposit", "main_menu.filter"};
        renderArgs.put("bagRemoved", (!Configuration.isIgnoreBag() && !ModelFacade.isIoBoardOk()));
        renderArgs.put("bagTotals", iq);
        renderArgs.put("bagFreeSpace", bagFreeSpace);
        renderArgs.put("checkPrinter", ModelFacade.printerNeedCheck());
        // I need space for at least one envelope. see ModelFacade->isBagReady too.
        iq.envelopes++;
        renderArgs.put("bagFull", Configuration.isBagFull(iq));
        checkMenu(back, backAction, buttons, titles, 0, extraButtons);
    }

    public static void otherMenu(String back) {
        String backAction = "MenuController.mainMenu";
        String[] buttons = {"CRUD.index", "MenuController.hardwareMenu", "MenuController.accountingMenu", "MenuController.reportMenu"};
        String[] titles = {"other_menu.db_admin", "other_menu.hardware_admin", "other_menu.accounting", "other_menu.reports"};
        checkMenu(back, backAction, buttons, titles, 1);
    }

    public static void hardwareMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"GloryController.index", "GloryManagerController.index", "IoBoardController.index",
            "CounterController.counterError", "PrinterController.listPrinters", "PrinterController.test"};
//            "CounterController.counterError", "PrinterController.listPrinters", "MenuController.printTemplateMenu"};
        String[] titles = {"other_menu.glory_cmd", "other_menu.glory_manager", "other_menu.ioboard_cmd", "other_menu.status",
            "other_menu.printer_list", "print_other_menu.test"};
//            "other_menu.printer_list", "other_menu.printer_test"};
        checkMenu(back, backAction, buttons, titles, 2);
    }

    public static void printTemplateMenu(String back) {
        String backAction = "MenuController.hardwareMenu";
        String[] buttons = {"PrinterController.billDeposit", "PrinterController.envelopeDeposit_finish",
            "PrinterController.envelopeDeposit_start", "PrinterController.test"};
        String[] titles = {"other_menu.print_billDeposit", "other_menu.print_envelopeDeposit_finish",
            "other_menu.print_envelopeDeposit_start", "print_other_menu.test"};
        checkMenu(back, backAction, buttons, titles, 3);
    }

    public static void accountingMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"ReportZController.print", "ReportZController.rotateZ", "ReportBagController.print", "ReportBagController.rotateBag"};
        String[] titles = {"other_menu.current_z_totals", "other_menu.rotate_z", "other_menu.current_bag_totals",
            "other_menu.rotate_bag"};
        checkMenu(back, backAction, buttons, titles, 2);
    }

    public static void reportMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"ReportDepositController.list", "ReportBagController.list", "ReportZController.list", "ReportEventController.list",
            "MenuController.unprocessedMenu"};
        String[] titles = {"other_menu.list_deposits", "other_menu.list_bags", "other_menu.list_zs", "other_menu.list_events",
            "other_menu.unprocessed_menu"};
        checkMenu(back, backAction, buttons, titles, 2);
    }

    public static void unprocessedMenu(String back) {
        String backAction = "MenuController.unprocessedMenu";
        String[] buttons = {"ReportController.unprocessedDeposits", "ReportController.unprocessedBags",
            "ReportController.unprocessedZs", "ReportController.unprocessedEvents"
        };
        String[] titles = {"other_menu.unprocessed_deposits", "other_menu.unprocessed_bags",
            "other_menu.unprocessed_zs", "other_menu.unprocessed_events"
        };
        checkMenu(back, backAction, buttons, titles, 2);
    }

    @Util
    static void checkMenu(String back, String backAction, String[] buttons, String[] titles, int level) {
        checkMenu(back, backAction, buttons, titles, level, null);
    }

    @Util
    static void checkMenu(String back, String backAction, String[] buttons, String[] titles, int level, String[] extraButtons) {
        Map<String, Boolean> perms = new HashMap<String, Boolean>();
        Map<String, String> ts = new HashMap<String, String>();
        int cnt = 0;
        String r = null;
        renderArgs.put("buttons", buttons);
        for (int i = 0; i < buttons.length; i++) {
            ts.put(buttons[i], titles[i]);
            boolean perm = Secure.checkPermission(buttons[i], "GET");
            perms.put(buttons[i], perm);
            if (perm) {
                cnt++;
                r = buttons[ i];
            }
        }
        if (extraButtons != null) {
            for (int i = 0; i < extraButtons.length; i++) {
                boolean perm = Secure.checkPermission(extraButtons[i], "GET");
                perms.put(extraButtons[i], perm);
                if (perm) {
                    cnt++;
                    r = extraButtons[i];
                }
            }
        }
        renderArgs.put("titles", ts);
        renderArgs.put("perms", perms);
        if (cnt == 1) {
            if (back != null) {
                if (level == 0) {
                    Secure.logout("Application.index");
                } else {
                    redirect(Router.getFullUrl(backAction));
                }
            } else {
                redirect(Router.getFullUrl(r));
            }
        } else {
            render();
        }
    }
}
