/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.security.SecureRandom;
import models.db.LgSystemProperty;
import static models.db.LgSystemProperty.setOrCreateProperty;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class Configuration {

    final public static int EXTERNAL_APP_ID = 2;

    public static boolean isPrinterTest() {
        return isSystemProperty("printer.test");
    }

    public static boolean isIgnorePrinter() {
        return isSystemProperty("printer.ignore");
    }

    public static boolean isIgnoreIoBoard() {
        return isSystemProperty("io_board.ignore");
    }

    public static boolean isIgnoreShutter() {
        if (isIgnoreIoBoard()) {
            return true;
        }
        return isSystemProperty("io_board.ignore_shutter");
    }

    public static boolean isIgnoreBag() {
        if (isIgnoreIoBoard()) {
            return true;
        }
        return isSystemProperty("io_board.ignore_bag");
    }

    public static boolean isReadyGate1(Integer gateval) {
        if (gateval == null || isIgnoreIoBoard() || isSystemProperty("io_board.ignore_gate_1")) {
            return true;
        }
        return 0 == (gateval & 0x1);
    }

    public static boolean isReadyGate2(Integer gateval) {
        if (gateval == null || isIgnoreIoBoard() || isSystemProperty("io_board.ignore_gate_2")) {
            return true;
        }
        return 0 == (gateval & 0x2);
    }

    public static boolean isReadyGateDoor(Integer gateval) {
        if (gateval == null || isIgnoreIoBoard() || isSystemProperty("io_board.ignore_gate_door")) {
            return true;
        }
        return 0 == (gateval & 0x4);
    }

    public static boolean isIgnoreGlory() {
        return isSystemProperty("glory.ignore");
    }

    public static String getProviderDescription() {
        String pc = getSystemProperty("application.provider_description");
        if (pc == null || pc.isEmpty()) {
            pc = getClientDescription();
        }
        return pc;
    }

    public static String getClientDescription() {
        return getSystemProperty("application.client_description");
    }

    public static Integer getDefaultCurrency() {
        try {
            return Integer.parseInt(getSystemProperty("application.default_currency"));
        } catch (NumberFormatException e) {
            Logger.warn("Fixing value 1 for application.default_currency");
            return 1;
        }
    }

    public static Boolean mustShowBillDepositReference1() {
        return isSystemProperty("bill_deposit.show_reference1");
    }

    public static Boolean mustShowBillDepositReference2() {
        return isSystemProperty("bill_deposit.show_reference2");
    }

    public static Boolean mustShowEnvelopeDepositReference1() {
        return isSystemProperty("envelope_deposit.show_reference1");
    }

    public static Boolean mustShowEnvelopeDepositReference2() {
        return isSystemProperty("envelope_deposit.show_reference2");
    }

    public static String getClientCode() {
        return getSystemProperty("application.client_code");
    }

    public static String getBranchCode() {
        return getSystemProperty("application.branch_code");
    }

    public static String getMachineCode() {
        return getSystemProperty("application.machine_code");
    }

    public static String getTicketFooter() {
        return getSystemProperty("application.ticket_footer");
    }

    public static String getTicketHeader() {
        return getSystemProperty("application.ticket_header");
    }

    public static boolean isPrintOnBagManualRotate() {
        return isSystemProperty("application.print_on_bag_auto");
    }

    public static boolean isPrintOnBagAutoRotate() {
        return isSystemProperty("application.print_on_bag_manual");
    }

    public static String getMachineDescription() {
        return getSystemProperty("application.machine_description");
    }

    public static boolean isAllAlowed() {
        return isProperty("secure.allowAll");
    }

    static public long maxBillsPerBag() {
        String d = getSystemProperty("glory.max_bills_per_bag");
        if (d == null) {
            return 15000;
        }
        return Long.parseLong(d);
    }

    static public long envelopeBillEquivalency() {
        String d = getSystemProperty("glory.envelope_bill_equivalency");
        if (d == null) {
            return 50;
        }
        return Long.parseLong(d);
    }

    static public long equivalentBillQuantity(Long bills, Long envelopes) {
        return bills + (Configuration.envelopeBillEquivalency() * envelopes);
    }

    static public boolean isBagFull(Long bills, Long envelopes) {
        /*Logger.debug("isBagFull : bills %d envelopes %d eq %d max %d",
         bills, envelopes,
         equivalentBillQuantity(bills, envelopes), Configuration.maxBillsPerBag());*/
        return (equivalentBillQuantity(bills, envelopes) >= Configuration.maxBillsPerBag());
    }

    public static String getWithdrawUser() {
        return getSystemProperty("application.withdraw_user");
    }

    public static int getBagPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.minBagLen"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getZPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.minZLen"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int getBillDepositPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.minBillDepositLen"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int getEvelopeFinishPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.minEnvelopeFinishLen"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int getEvenlopeStartPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.minEnvelopeStartLen"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getPrintWidth() {
        try {
            return Integer.parseInt(getSystemProperty("print.maxPaperWidth"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getGloryPort() {
        return getProperty("glory.port");
    }

    public static String getIoBoardPort() {
        return getProperty("io_board.port");
    }

    public static String getIoBoardVersion() {
        return getProperty("io_board.version");
    }

    public static boolean isCrapAuth() {
        return isSystemProperty("secure.crapAuth");
    }

    public static boolean dontAskForPassword() {
        return isProperty("secure.dontAskForPassword");
    }

    public static boolean wellcomePopup() {
        return isProperty("secure.wellcomePopup");
    }

    public static boolean useHardwareKeyboard() {
        return isProperty("style.useHardwareKeyboard");
    }

    public static String getDefaultPrinter() {
        String pc = getSystemProperty("printer.port");
        return pc;
    }

    static void setDefaultPrinter(String prt) {
        LgSystemProperty.setOrCreateProperty("printer.port", prt);
    }

    public static String getErrorStr() {
        return getSystemProperty("application.error_msg");
    }

    public static void initCrapId() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[3];
        random.nextBytes(bytes);
        int val = 0;
        for (int i = 0; i < 3; i++) {
            val = val * 100;
            val += ((((int) bytes[i] & 0x0F) % 9) + 1) * 10 + (((((int) bytes[i] & 0xF0) >> 4) % 9) + 1);
        }
        setOrCreateProperty("secure.crapauth_variable_id", Integer.toString(val));
    }

    public static String getCrapAuthId() {
        return getSystemProperty("secure.crapauth_variable_id");
    }

    public static String getCrapAuthConstantId() {
        return getSystemProperty("secure.crapauth_constant_id");
    }

    private static String getSystemProperty(String property) {
        LgSystemProperty p = LgSystemProperty.getProperty(property);
        if (p != null && p.value != null && !p.value.isEmpty()) {
            return p.value;
        } else {
            return getProperty(property);
        }
    }

    private static String getProperty(String property) {
        return Play.configuration.getProperty(property);
    }

    private static boolean isSystemProperty(String property) {
        LgSystemProperty p = LgSystemProperty.getProperty(property);
        if (p != null && !p.value.isEmpty()) {
            if (p.value.trim().equalsIgnoreCase("true") || p.value.trim().equalsIgnoreCase("on")) {
                return true;
            }
            return false;
        } else {
            return isProperty(property);
        }
    }

    private static boolean isProperty(String property) {
        String prop = Play.configuration.getProperty(property);
        if (prop != null) {
            if (prop.trim().equalsIgnoreCase("true") || prop.trim().equalsIgnoreCase("on")) {
                return true;
            }
        }
        return false;
    }

}
