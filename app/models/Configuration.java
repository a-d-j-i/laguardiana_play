/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

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

    public static boolean dontAskForPassword() {
        return isProperty("secure.dontAskForPassword");
    }

    public static boolean isIoBoardIgnore() {
        return isProperty("io_board.ignore") && Play.mode.isDev();
    }

    public static boolean isIgnoreShutter() {
        return isProperty("io_board.ignore_shutter");
    }

    public static boolean isGloryIgnore() {
        return isProperty("glory.ignore") && Play.mode.isDev();
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

    public static Boolean mustShowReference1() {
        return LgSystemProperty.isProperty("bill_deposit.show_reference1");
    }

    public static Boolean mustShowReference2() {
        return LgSystemProperty.isProperty("bill_deposit.show_reference2");
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
}
