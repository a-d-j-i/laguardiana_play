package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Configuration;
import models.ItemQuantity;
import models.ModelFacade;
import models.ModelFacade.IoBoardCondition;
import models.db.LgBag;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class MenuController extends Controller {

    final static public class MenuButton {

        final public String action;
        final public String title;
        final public boolean auto_navigate;

        public MenuButton(String action, String title) {
            this(action, title, true);
        }

        public MenuButton(String action, String title, boolean auto_navigate) {
            this.action = action;
            this.title = title;
            this.auto_navigate = auto_navigate;
        }

    }

    public static void mainMenu(String back) {
        LgBag currentBag = LgBag.getCurrentBag();
        ItemQuantity iq = currentBag.getItemQuantity();
        //Logger.debug("Item quantity : %s", iq.toString());
        Long bagFreeSpace = Configuration.maxBillsPerBag() - Configuration.equivalentBillQuantity(iq.bills, iq.envelopes);
        if (bagFreeSpace < 0) {
            bagFreeSpace = (long) 0;
        }
        boolean isBagFull = Configuration.isBagFull(iq.bills - 1, iq.envelopes + 1);
        IoBoardCondition bagReadyCondition = ModelFacade.ioBoardReadyCondition();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = ModelFacade.printerNeedCheck();
            o[1] = bagReadyCondition;
            // I need space for at least one envelope. see ModelFacade->isBagReady too.
            o[2] = isBagFull;
            renderJSON(o);
        }
        renderArgs.put("bagReadyCondition", bagReadyCondition);
        renderArgs.put("bagTotals", iq);
        renderArgs.put("bagFreeSpace", bagFreeSpace);
        renderArgs.put("checkPrinter", ModelFacade.printerNeedCheck());
        renderArgs.put("lockedByUser", ModelFacade.getLockedByUser());
        // I need space for at least one envelope. see ModelFacade->isBagReady too.
        renderArgs.put("bagFull", isBagFull);

        String backAction = "MenuController.mainMenu";
        final MenuButton[] buttons = {new MenuButton("BillDepositController.start", "main_menu.cash_deposit"),
            new MenuButton("CountController.start", "main_menu.count"),
            new MenuButton("EnvelopeDepositController.start", "main_menu.envelope_deposit"),
            new MenuButton("FilterController.start", "main_menu.filter"),
            new MenuButton("IoBoardController.unlockDoor", "application.unlock_door", false)
        };
        final MenuButton[] extraButtons = {new MenuButton("MenuController.otherMenu", "main_menu.other_menu", false)};
        String nextStep = renderMenuButtons(buttons, extraButtons);
        if (nextStep == null || !bagReadyCondition.ready) {
            render();
        } else if (back != null) {
            Secure.logout("Application.index");
        } else {
            Logger.debug("Main menu nextstep: %s", nextStep);
            redirect(Router.getFullUrl(nextStep));
        }
    }

    public static void otherMenu(String back) {
        String backAction = "MenuController.mainMenu";
        final MenuButton[] buttons = {new MenuButton("MenuController.hardwareMenu", "other_menu.hardware_admin"),
            new MenuButton("MenuController.accountingMenu", "other_menu.accounting"),
            new MenuButton("MenuController.reportMenu", "other_menu.reports")
        };
        String s = Configuration.getVersion();
        if (s != null) {
            renderArgs.put("release", s);
        }
        renderMenuAndNavigate(back, backAction, buttons);
    }

    public static void hardwareMenu(String back) {
        String backAction = "MenuController.otherMenu";
        final MenuButton[] buttons = {new MenuButton("GloryController.index", "other_menu.glory_cmd"),
            new MenuButton("GloryManagerController.index", "other_menu.glory_manager"),
            new MenuButton("IoBoardController.index", "other_menu.ioboard_cmd"),
            new MenuButton("ConfigController.status", "other_menu.status"),
            new MenuButton("PrinterController.listPrinters", "other_menu.printer_list"),
            new MenuButton("ConfigController.index", "other_menu.config")
        };
        renderMenuAndNavigate(back, backAction, buttons);
    }

    public static void printTemplateMenu(String back) {
        String backAction = "MenuController.hardwareMenu";
        final MenuButton[] buttons = {new MenuButton("PrinterController.billDeposit", "other_menu.print_billDeposit"),
            new MenuButton("PrinterController.envelopeDeposit_finish", "other_menu.print_envelopeDeposit_finish"),
            new MenuButton("PrinterController.envelopeDeposit_start", "other_menu.print_envelopeDeposit_start"),
            new MenuButton("PrinterController.test", "print_other_menu.test")
        };
        renderMenuAndNavigate(back, backAction, buttons);
    }

    public static void accountingMenu(String back) {
        String backAction = "MenuController.otherMenu";
        final MenuButton[] buttons = {new MenuButton("ReportZController.print", "other_menu.current_z_totals"),
            new MenuButton("ReportZController.rotateZ", "other_menu.rotate_z"),
            new MenuButton("ReportBagController.print", "other_menu.current_bag_totals"),
            new MenuButton("ReportBagController.rotateBag", "other_menu.rotate_bag")
        };
        renderMenuAndNavigate(back, backAction, buttons);
    }

    public static void reportMenu(String back) {
        String backAction = "MenuController.otherMenu";
        final MenuButton[] buttons = {new MenuButton("ReportDepositController.list", "other_menu.list_deposits"),
            new MenuButton("ReportBagController.list", "other_menu.list_bags"),
            new MenuButton("ReportZController.list", "other_menu.list_zs"),
            new MenuButton("ReportEventController.list", "other_menu.list_events"),
            new MenuButton("MenuController.unprocessedMenu", "other_menu.unprocessed_menu")
        };
        renderMenuAndNavigate(back, backAction, buttons);
    }

    public static void unprocessedMenu(String back) {
        String backAction = "MenuController.unprocessedMenu";
        final MenuButton[] buttons = {new MenuButton("ReportController.unprocessedDeposits", "other_menu.unprocessed_deposits"),
            new MenuButton("ReportController.unprocessedBags", "other_menu.unprocessed_bags"),
            new MenuButton("ReportController.unprocessedZs", "other_menu.unprocessed_zs"),
            new MenuButton("ReportController.unprocessedEvents", "other_menu.unprocessed_events")
        };
        renderMenuAndNavigate(back, backAction, buttons);
    }

    @Util
    static String renderMenuButtons(MenuButton[] buttons, MenuButton[] extraButtons) {
        int cnt = 0;
        Map<String, Boolean> perms = new HashMap<String, Boolean>();
        List<MenuButton> ts = new ArrayList<MenuButton>();
        MenuButton r = null;
        renderArgs.put("buttons", buttons);
        for (int i = 0; i < buttons.length; i++) {
            ts.add(buttons[i]);
            boolean perm = Secure.checkPermission(buttons[i].action, "GET");
            perms.put(buttons[i].action, perm);
            if (perm) {
                cnt++;
                r = buttons[i];
            }
        }
        if (extraButtons != null) {
            for (int i = 0; i < extraButtons.length; i++) {
                boolean perm = Secure.checkPermission(extraButtons[i].action, "GET");
                perms.put(extraButtons[i].action, perm);
                if (perm) {
                    cnt++;
                    r = extraButtons[i];
                }
            }
        }
        renderArgs.put("titles", ts);
        renderArgs.put("perms", perms);
        if (cnt == 1 && r != null && r.auto_navigate) {
            return r.action;
        }
        return null;
    }

    @Util
    static void renderMenuAndNavigate(String back, String backAction, MenuButton[] buttons) {
        String nextStep = renderMenuButtons(buttons, null);
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
