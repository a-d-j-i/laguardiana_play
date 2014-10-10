/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.serial.SerialPortAdapterAbstract.PortConfiguration;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.implementations.SerialPortAdapterRxTx;
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

    public static String getMachineType() {
        return getSystemProperty("application.machine_type");
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
            return Integer.parseInt(getSystemProperty("print.bagLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    public static int getZPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.zLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getBillDepositPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.billDepositLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getEvelopeFinishPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.envelopeFinishLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    static int getEvenlopeStartPrintLen() {
        try {
            return Integer.parseInt(getSystemProperty("print.envelopeStartLen"));
        } catch (NumberFormatException e) {
            return 220;
        }
    }

    public static int getPrintWidth() {
        try {
            return Integer.parseInt(getSystemProperty("print.paperWidth"));
        } catch (NumberFormatException e) {
            return 77;
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

    public static String getErrorStr() {
        return getSystemProperty("application.error_msg");
    }

    private static void initCrapId() {
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
    private static boolean crapIdInitialized = false;

    public static String getCrapAuthId() {
        if (!crapIdInitialized) {
            crapIdInitialized = true;
            initCrapId();
        }
        return getSystemProperty("secure.crapauth_variable_id");
    }

    public static String getCrapAuthConstantId() {
        return getSystemProperty("secure.crapauth_constant_id");
    }

    public static String getSystemProperty(String property) {
        LgSystemProperty p = LgSystemProperty.getProperty(property);
        if (p != null && !p.value.isEmpty()) {
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

    public static SerialPortAdapterInterface getSerialPort(String port, PortConfiguration conf) {
        //SerialPortAdapterInterface serialPort = new SerialPortAdapterJSSC( port );
        return new SerialPortAdapterRxTx(port, conf);
    }
}
