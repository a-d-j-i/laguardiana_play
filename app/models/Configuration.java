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

    public static boolean ioBoardIgnore() {
        return Play.configuration.getProperty("io_board.ignore") == null;
    }

    public static Object getClientDescription() {
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

    public static Object useHardwareKeyboard() {
        return LgSystemProperty.isProperty("useHardwareKeyboard");
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
}
