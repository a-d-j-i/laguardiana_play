/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printer;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.INT_PTR;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.Winspool.PRINTER_INFO_1;
import com.sun.jna.platform.win32.Winspool.PRINTER_INFO_4;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import devices.printer.WinSpool.WinspoolLib.PRINTER_INFO_2;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class WinSpool {

    public enum WinSpoolPrinterStatus {

        //PRINTER_STATUS_READY("The printer is ready", 0),
        PRINTER_STATUS_BUSY("The printer is busy", 0x00000200),
        PRINTER_STATUS_DOOR_OPEN("The printer door is open", 0x00400000),
        PRINTER_STATUS_ERROR("The printer is in an error state.", 0x00000002),
        PRINTER_STATUS_INITIALIZING("The printer is initializing.", 0x00008000),
        PRINTER_STATUS_IO_ACTIVE("The printer is in an active input/output state", 0x00000100),
        PRINTER_STATUS_MANUAL_FEED("The printer is in a manual feed state.", 0x00000020),
        PRINTER_STATUS_NO_TONER("The printer is out of toner.", 0x00040000),
        PRINTER_STATUS_NOT_AVAILABLE("The printer is not available for printing.", 0x00001000),
        PRINTER_STATUS_OFFLINE("The printer is offline.", 0x00000080),
        PRINTER_STATUS_OUT_OF_MEMORY("The printer has run out of memory.", 0x00200000),
        PRINTER_STATUS_OUTPUT_BIN_FULL("The printer's output bin is full.", 0x00000800),
        PRINTER_STATUS_PAGE_PUNT("The printer cannot print the current page.", 0x00080000),
        PRINTER_STATUS_PAPER_JAM("Paper is jammed in the printer", 0x00000008),
        PRINTER_STATUS_PAPER_OUT("The printer is out of paper.", 0x00000010),
        PRINTER_STATUS_PAPER_PROBLEM("The printer has a paper problem.", 0x00000040),
        PRINTER_STATUS_PAUSED("The printer is paused.", 0x00000001),
        PRINTER_STATUS_PENDING_DELETION("The printer is being deleted.", 0x00000004),
        PRINTER_STATUS_POWER_SAVE("The printer is in power save mode.", 0x01000000),
        PRINTER_STATUS_PRINTING("The printer is printing.", 0x00000400),
        PRINTER_STATUS_PROCESSING("The printer is processing a print job.", 0x00004000),
        PRINTER_STATUS_SERVER_UNKNOWN("The printer status is unknown.", 0x00800000),
        PRINTER_STATUS_TONER_LOW("The printer is low on toner.", 0x00020000),
        PRINTER_STATUS_USER_INTERVENTION("The printer has an error that requires the user to do something.", 0x00100000),
        PRINTER_STATUS_WAITING("The printer is waiting.", 0x00002000),
        PRINTER_STATUS_WARMING_UP("The printer is warming up.", 0x00010000),
        PRINTER_STATUS_APP_ERROR("An application error calling getStatus.", 0xFFFFFFFF);

        static public Set<WinSpoolPrinterStatus> getStatusBits(int b) {
            EnumSet<WinSpoolPrinterStatus> hs = EnumSet.noneOf(WinSpoolPrinterStatus.class);
            for (WinSpoolPrinterStatus s : WinSpoolPrinterStatus.values()) {
                if ((b & s.stat) != 0) {
                    hs.add(s);
                }
            }
            return hs;
        }
        final private int stat;
        final private String desc;

        private WinSpoolPrinterStatus(String desc, int stat) {
            this.desc = desc;
            this.stat = stat;
        }

        public String getDesc() {
            return desc;
        }
    }

    public interface WinspoolLib extends StdCallLibrary {

        WinspoolLib INSTANCE = (WinspoolLib) Native.loadLibrary("Winspool.drv", WinspoolLib.class, W32APIOptions.UNICODE_OPTIONS);

        boolean EnumPrinters(int Flags, String Name, int Level, Pointer pPrinterEnum,
                int cbBuf, IntByReference pcbNeeded, IntByReference pcReturned);

        boolean GetPrinter(HANDLE hPrinter, int Level, Pointer pPrinter, int cbBuf, IntByReference pcbNeeded);

        boolean OpenPrinter(String pPrinterName, HANDLEByReference phPrinter, Pointer pDefault);

        public static class PRINTER_INFO_1 extends Structure {

            public int Flags;
            public String pDescription;
            public String pName;
            public String pComment;

            protected List<String> getFieldOrder() {
                return Arrays.asList(new String[]{"Flags", "pDescription", "pName", "pComment"});
            }

            public PRINTER_INFO_1() {
            }

            public PRINTER_INFO_1(int size) {
                super(new Memory(size));
            }
        }

        public static class PRINTER_INFO_2 extends Structure {

            public String pServerName;
            public String pPrinterName;
            public String pShareName;
            public String pPortName;
            public String pDriverName;
            public String pComment;
            public String pLocation;
            public INT_PTR pDevMode;
            public String pSepFile;
            public String pPrintProcessor;
            public String pDatatype;
            public String pParameters;
            public INT_PTR pSecurityDescriptor;
            public int Attributes;
            public int Priority;
            public int DefaultPriority;
            public int StartTime;
            public int UntilTime;
            public int Status;
            public int cJobs;
            public int AveragePPM;

            protected List<String> getFieldOrder() {
                return Arrays.asList(new String[]{"pServerName", "pPrinterName", "pShareName", "pPortName",
                            "pDriverName", "pComment", "pLocation", "pDevMode", "pSepFile", "pPrintProcessor",
                            "pDatatype", "pParameters", "pSecurityDescriptor", "Attributes", "Priority", "DefaultPriority",
                            "StartTime", "UntilTime", "Status", "cJobs", "AveragePPM"});
            }

            public PRINTER_INFO_2() {
            }

            public PRINTER_INFO_2(int size) {
                super(new Memory(size));
            }
        }

        public static class PRINTER_INFO_4 extends Structure {

            public String pPrinterName;
            public String pServerName;
            public DWORD Attributes;

            protected List<String> getFieldOrder() {
                return Arrays.asList(new String[]{"pPrinterName", "pServerName", "Attributes"});
            }

            public PRINTER_INFO_4() {
            }

            public PRINTER_INFO_4(int size) {
                super(new Memory(size));
            }
        }
        int PRINTER_ENUM_DEFAULT = 0x00000001;
        int PRINTER_ENUM_LOCAL = 0x00000002;
        int PRINTER_ENUM_CONNECTIONS = 0x00000004;
        int PRINTER_ENUM_FAVORITE = 0x00000004;
        int PRINTER_ENUM_NAME = 0x00000008;
        int PRINTER_ENUM_REMOTE = 0x00000010;
        int PRINTER_ENUM_SHARED = 0x00000020;
        int PRINTER_ENUM_NETWORK = 0x00000040;
        int PRINTER_ENUM_EXPAND = 0x00004000;
        int PRINTER_ENUM_CONTAINER = 0x00008000;
        int PRINTER_ENUM_ICONMASK = 0x00ff0000;
        int PRINTER_ENUM_ICON1 = 0x00010000;
        int PRINTER_ENUM_ICON2 = 0x00020000;
        int PRINTER_ENUM_ICON3 = 0x00040000;
        int PRINTER_ENUM_ICON4 = 0x00080000;
        int PRINTER_ENUM_ICON5 = 0x00100000;
        int PRINTER_ENUM_ICON6 = 0x00200000;
        int PRINTER_ENUM_ICON7 = 0x00400000;
        int PRINTER_ENUM_ICON8 = 0x00800000;
        int PRINTER_ENUM_HIDE = 0x01000000;
    }

    public static PRINTER_INFO_1[] getPrinterInfo1() {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 1, null, 0, pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_1[0];
        }

        PRINTER_INFO_1 pPrinterEnum = new PRINTER_INFO_1(pcbNeeded.getValue());
        if (!WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 1, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded, pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        pPrinterEnum.read();

        return (PRINTER_INFO_1[]) pPrinterEnum.toArray(pcReturned.getValue());
    }

    public static PRINTER_INFO_2[] getPrinterInfo2() {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 2, null, 0, pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_2[0];
        }

        PRINTER_INFO_2 pPrinterEnum = new PRINTER_INFO_2(pcbNeeded.getValue());
        if (!WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 2, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded, pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        pPrinterEnum.read();
        return (PRINTER_INFO_2[]) pPrinterEnum.toArray(pcReturned.getValue());
    }

    public static PRINTER_INFO_4[] getPrinterInfo4() {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 4, null, 0, pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_4[0];
        }

        PRINTER_INFO_4 pPrinterEnum = new PRINTER_INFO_4(pcbNeeded.getValue());
        if (!WinspoolLib.INSTANCE.EnumPrinters(WinspoolLib.PRINTER_ENUM_LOCAL,
                null, 4, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded, pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        pPrinterEnum.read();

        return (PRINTER_INFO_4[]) pPrinterEnum.toArray(pcReturned.getValue());
    }

    public static PRINTER_INFO_2 getPrinterInfo2(String printerName) {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        HANDLEByReference pHandle = new HANDLEByReference();

        WinspoolLib.INSTANCE.OpenPrinter(printerName, pHandle, null);

        WinspoolLib.INSTANCE.GetPrinter(pHandle.getValue(), 2, null, 0, pcbNeeded);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_2();
        }

        PRINTER_INFO_2 pinfo2 = new PRINTER_INFO_2(pcbNeeded.getValue());

        WinspoolLib.INSTANCE.GetPrinter(pHandle.getValue(), 2, pinfo2.getPointer(), pcbNeeded.getValue(), pcReturned);

        pinfo2.read();
        return (PRINTER_INFO_2) pinfo2;
    }

    /*    public static WinSpoolPrinterStatus getPrinterStatus(String printerName) {
     PRINTER_INFO_2 pi = getPrinterInfo2(printerName);
     if (pi == null) {
     return WinSpoolPrinterStatus.PRINTER_STATUS_APP_ERROR;
     }
     return WinSpoolPrinterStatus.getStatus(pi.Status);
     }*/
    static void refreshState(String name, Printer.State state) {
        PRINTER_INFO_2 pi = getPrinterInfo2(name);
        if (pi == null) {
            state.setState(Printer.PRINTER_STATE.PRINTER_SPOOL_PROBLEM, "Printer info is null");
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "Printer info is null"));
            return;
        }

        Set hs = WinSpoolPrinterStatus.getStatusBits(pi.Status);

        if (hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_SERVER_UNKNOWN)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PENDING_DELETION)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_NOT_AVAILABLE)) {
            state.setState(Printer.PRINTER_STATE.PRINTER_SPOOL_PROBLEM, hs.toString());
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, hs.toString()));
            return;
        }
        state.clearError();

        if (hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_OFFLINE)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_DOOR_OPEN)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_ERROR)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_OUT_OF_MEMORY)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_NO_TONER)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_OUTPUT_BIN_FULL)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PAPER_JAM)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PAPER_OUT)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PAPER_PROBLEM)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PAUSED)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_POWER_SAVE)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_OUTPUT_BIN_FULL)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_TONER_LOW)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_USER_INTERVENTION)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_OUTPUT_BIN_FULL)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PAGE_PUNT)) {
            state.setState(Printer.PRINTER_STATE.PRINTER_SPOOL_PROBLEM, hs.toString());
        } else if (hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_BUSY)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_WARMING_UP)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_INITIALIZING)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_IO_ACTIVE)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_MANUAL_FEED)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PRINTING)
                || hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_PROCESSING)) {
            state.setState(Printer.PRINTER_STATE.PRINTER_PRINTING, "Printing " + hs.toString());
        } else {
            //if (hs.contains(WinSpoolPrinterStatus.PRINTER_STATUS_WAITING)) {
            state.setState(Printer.PRINTER_STATE.PRINTER_READY, "Ready");
        }
    }
}
