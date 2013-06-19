/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import models.db.LgSystemProperty;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class Configuration {

    final public static int EXTERNAL_APP_ID = 2;

    public static boolean isPrinterTest() {
        return isProperty("printer.test");
    }

    public static boolean isIgnorePrinter() {
        return isProperty("printer.ignore");
    }

    public static boolean dontAskForPassword() {
        return isProperty("secure.dontAskForPassword");
    }

    public static boolean isIoBoardIgnore() {
        return isProperty("io_board.ignore") && Play.mode.isDev();
    }

    public static boolean isIgnoreShutter() {
        if (isIoBoardIgnore()) {
            return true;
        }
        return isProperty("io_board.ignore_shutter");
    }

    public static boolean isIgnoreBag() {
        if (isIoBoardIgnore()) {
            return true;
        }
        return isProperty("io_board.ignore_bag");
    }

    public static boolean isGloryIgnore() {
        return isProperty("glory.ignore") && Play.mode.isDev();
    }

    static boolean isPrinterIgnore() {
        return isProperty("printer.ignore") && Play.mode.isDev();

    }

    public static String getProviderDescription() {
        String pc = LgSystemProperty.getProperty(LgSystemProperty.Types.PROVIDER_DESCRIPTION);
        if (pc == null || pc.isEmpty()) {
            pc = getClientDescription();
        }
        return pc;
    }

    public static String getClientDescription() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_DESCRIPTION);
    }

    public static Integer getDefaultCurrency() {
        return getIntProperty(LgSystemProperty.Types.DEFAULT_CURRENCY);
    }

    static private Integer getIntProperty(LgSystemProperty.Types type) {
        try {
            return Integer.parseInt(LgSystemProperty.getProperty(type));
        } catch (NumberFormatException e) {
            Logger.warn("Fixing value 1 parsing a int property");
            // fixed value
            return 1;
        }
    }

    public static Boolean mustShowBillDepositReference1() {
        return LgSystemProperty.isProperty("bill_deposit.show_reference1");
    }

    public static Boolean mustShowBillDepositReference2() {
        return LgSystemProperty.isProperty("bill_deposit.show_reference2");
    }

    public static Boolean mustShowEnvelopeDepositReference1() {
        return LgSystemProperty.isProperty("envelope_deposit.show_reference1");
    }

    public static Boolean mustShowEnvelopeDepositReference2() {
        return LgSystemProperty.isProperty("envelope_deposit.show_reference2");
    }

    public static String getClientCode() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE);
    }

    public static String getBranchCode() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.BRANCH_CODE);
    }

    public static String getMachineCode() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_CODE);
    }

    public static String getMachineDescription() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_DESCRIPTION);
    }

    public static boolean isAllAlowed() {
        return isProperty("secure.allowAll");
    }

    private static boolean isProperty(String property) {
        String prop = Play.configuration.getProperty(property);
        if (prop == null) {
            return false;
        }
        if (prop.equalsIgnoreCase("true") || prop.equalsIgnoreCase("on")) {
            return true;
        }
        return false;
    }

    static public long maxBillsPerBag() {
        String d = LgSystemProperty.getProperty(LgSystemProperty.Types.MAX_BILLS_PER_BAG);
        if (d == null) {
            d = Play.configuration.getProperty("glory.maxBillsPerBag");
        }
        if (d == null) {
            return 15000;
        }
        return Long.parseLong(d);
    }

    static public long envelopeBillEquivalency() {
        String d = LgSystemProperty.getProperty(LgSystemProperty.Types.ENVELOPE_BILL_EQUIVALENCY);
        if (d == null) {
            d = Play.configuration.getProperty("glory.envelopeBillEquivalency");
        }
        if (d == null) {
            return 50;
        }
        return Long.parseLong(d);
    }

    static public long equivalentBillQuantity(Long billQuantity, Long envelopeQuantity) {
        return billQuantity + (Configuration.envelopeBillEquivalency() * envelopeQuantity);
    }

    static public boolean isBagFull(Long billQuantity, Long envelopeQuantity) {
        return (equivalentBillQuantity(billQuantity, envelopeQuantity) > Configuration.maxBillsPerBag());
    }

    public static String getWithdrawUser() {
        return LgSystemProperty.getProperty(LgSystemProperty.Types.WITHDRAW_USER);
    }

    public static int getBagPrintLen() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.bagLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    public static int getZPrintLen() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.zLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getBillDepositPrintLen() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.billDepositLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getEvelopeFinishPrintLen() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.envelopeFinishLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getEvenlopeStartPrintLen() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.envelopeStartLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    public static int getPrintWidth() {
        try {
            return Integer.parseInt(Play.configuration.getProperty("print.paperWidth"));
        } catch (NumberFormatException e) {
            return 77;
        }
    }
}
