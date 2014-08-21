package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import models.Configuration;
import models.ItemQuantity;
import models.ModelFacade;
import models.db.LgBag;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class MenuController extends Controller {

    public static void mainMenu(String back) {
        LgBag currentBag = LgBag.getCurrentBag();
        ItemQuantity iq = currentBag.getItemQuantity();
        //Logger.debug("Item quantity : %s", iq.toString());
        Long bagFreeSpace = Configuration.maxBillsPerBag() - Configuration.equivalentBillQuantity(iq.bills, iq.envelopes);
        if (bagFreeSpace < 0) {
            bagFreeSpace = (long) 0;
        }
        boolean isBagFull = Configuration.isBagFull(iq.bills - 1, iq.envelopes + 1);
        boolean isBagRemoved = !Configuration.isIgnoreBag() && !ModelFacade.isBagReady();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = ModelFacade.isReadyToPrint();
            o[1] = isBagRemoved;
            // I need space for at least one envelope. see ModelFacade->isBagReady too.
            o[2] = isBagFull;
            renderJSON(o);
        }
        renderArgs.put("bagRemoved", isBagRemoved);
        renderArgs.put("bagTotals", iq);
        renderArgs.put("bagFreeSpace", bagFreeSpace);
        renderArgs.put("checkPrinter", ModelFacade.isReadyToPrint());
        // I need space for at least one envelope. see ModelFacade->isBagReady too.
        renderArgs.put("bagFull", isBagFull);

        String backAction = "MenuController.mainMenu";
        String[] buttons = {"BillDepositController.start", "CountController.start", "EnvelopeDepositController.start", "FilterController.start"};
        String[] extraButtons = {"MenuController.otherMenu"};
        String[] titles = {"main_menu.cash_deposit", "main_menu.count", "main_menu.envelope_deposit", "main_menu.filter"};
        String nextStep = renderMenuButtons(buttons, titles, extraButtons);
        if (nextStep == null || isBagRemoved) {
            render();
        } else {
            if (back != null) {
                Secure.logout("Application.index");
            } else {
                redirect(Router.getFullUrl(nextStep));
            }
        }
    }

    public static void otherMenu(String back) {
        String backAction = "MenuController.mainMenu";
        String[] buttons = {"MenuController.hardwareMenu", "MenuController.accountingMenu", "MenuController.reportMenu", "ConfigController.index"};
        String[] titles = {"other_menu.hardware_admin", "other_menu.accounting", "other_menu.reports", "other_menu.config"};
        File f = play.Play.getFile(".hg_archival.txt");
        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            String s = new String(data, "UTF-8");
            s = s.substring(s.indexOf("latesttag:") + "latesttag:".length(), s.lastIndexOf("latesttag"));
            renderArgs.put("release", s);
        } catch (StringIndexOutOfBoundsException ex) {
            //Logger.error("Error reading release file : %s", ex.toString());
        } catch (IOException ex) {
            //Logger.error("Error reading release file : %s", ex.toString());
        }

        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    public static void hardwareMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"DeviceController.list", "IoBoardController.index", "PrinterController.listPrinters",};
//            "ConfigController.status", "PrinterController.listPrinters", "MenuController.printTemplateMenu"};
        String[] titles = {"other_menu.devices", "other_menu.ioboard_cmd", "other_menu.printer_list"};
//            "other_menu.printer_list", "other_menu.printer_test"};
        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    public static void printTemplateMenu(String back) {
        String backAction = "MenuController.hardwareMenu";
        String[] buttons = {"PrinterController.billDeposit", "PrinterController.envelopeDeposit_finish",
            "PrinterController.envelopeDeposit_start", "PrinterController.test"};
        String[] titles = {"other_menu.print_billDeposit", "other_menu.print_envelopeDeposit_finish",
            "other_menu.print_envelopeDeposit_start", "print_other_menu.test"};
        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    public static void accountingMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"ReportZController.print", "ReportZController.rotateZ", "ReportBagController.print", "ReportBagController.rotateBag"};
        String[] titles = {"other_menu.current_z_totals", "other_menu.rotate_z", "other_menu.current_bag_totals",
            "other_menu.rotate_bag"};
        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    public static void reportMenu(String back) {
        String backAction = "MenuController.otherMenu";
        String[] buttons = {"ReportDepositController.list", "ReportBagController.list", "ReportZController.list", "ReportEventController.list",
            "MenuController.unprocessedMenu"};
        String[] titles = {"other_menu.list_deposits", "other_menu.list_bags", "other_menu.list_zs", "other_menu.list_events",
            "other_menu.unprocessed_menu"};
        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    public static void unprocessedMenu(String back) {
        String backAction = "MenuController.unprocessedMenu";
        String[] buttons = {"ReportController.unprocessedDeposits", "ReportController.unprocessedBags",
            "ReportController.unprocessedZs", "ReportController.unprocessedEvents"
        };
        String[] titles = {"other_menu.unprocessed_deposits", "other_menu.unprocessed_bags",
            "other_menu.unprocessed_zs", "other_menu.unprocessed_events"
        };
        renderMenuAndNavigate(back, backAction, buttons, titles, null);
    }

    @Util
    static String renderMenuButtons(String[] buttons, String[] titles, String[] extraButtons) {
        int cnt = 0;
        Map<String, Boolean> perms = new HashMap<String, Boolean>();
        Map<String, String> ts = new HashMap<String, String>();
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
            return r;
        }
        return null;
    }

    @Util
    static void renderMenuAndNavigate(String back, String backAction, String[] buttons, String[] titles, String[] extraButtons) {
        String nextStep = renderMenuButtons(buttons, titles, extraButtons);
        if (nextStep != null) {
            if (back != null) {
                redirect(Router.getFullUrl(backAction));
            } else {
                redirect(Router.getFullUrl(nextStep));
            }
        }
        render();
    }
}
