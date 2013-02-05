package controllers;

import java.util.HashMap;
import java.util.Map;
import play.mvc.*;

@With({Secure.class})
public class MenuController extends Controller {

    public static void mainMenu(String back) {
        String backAction = "MenuController.mainMenu";
        String[] buttons = {"BillDepositController.start", "CountController.start", "EnvelopeDepositController.start", "FilterController.start"};
        String[] titles = {"main_menu.cash_deposit", "main_menu.count", "main_menu.envelope_deposit", "main_menu.filter"};
        checkMenu(back, backAction, buttons, titles, 0);
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
            "CounterController.counterError", "PrinterController.listPrinters", "MenuController.printTemplateMenu"};
        String[] titles = {"other_menu.glory_cmd", "other_menu.glory_manager", "other_menu.ioboard_cmd", "other_menu.status",
            "other_menu.printer_list", "other_menu.printer_test"};
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
        String[] buttons = {"ReportDepositController.unprocessed", "ReportBagController.unprocessed", "ReportZController.unprocessed",
            "ReportDepositController.list", "ReportBagController.list", "ReportZController.list"};
        String[] titles = {"other_menu.unprocessed_deposits", "other_menu.unprocessed_bags", "other_menu.unprocessed_zs",
            "other_menu.list_deposits", "other_menu.list_bags", "other_menu.list_zs"};
        checkMenu(back, backAction, buttons, titles, 2);
    }

    @Util
    static void checkMenu(String back, String backAction, String[] buttons, String[] titles, int level) {
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
