package controllers;

import devices.glory.operation.EndUpload;
import devices.glory.operation.LogDataRequest;
import devices.glory.operation.StartUpload;
import devices.glory.operation.Sense;
import devices.device.DeviceInterface;
import devices.glory.GloryDE50Device;
import devices.glory.task.GloryDE50TaskOperation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import machines.Machine;
import play.Logger;
import play.mvc.Before;

public class GloryDE50Controller extends Application {

    static GloryDE50Device glory;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        if (deviceId == null) {
            DeviceController.list();
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d instanceof GloryDE50Device) {
            glory = (GloryDE50Device) d;
        } else {
            renderArgs.put("error", "invalid device id");
            setStatusAndRedirect(deviceId, null);
        }
    }

    private static void setStatusAndRedirect(Integer deviceId, GloryDE50TaskOperation op) {
        // TODO: op.isError();
        if (op.getResponse() != null) {
            Logger.debug("STATUS : %s", op.getResponse().toString());
            renderArgs.put("status", op.getResponse().getRepr());
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getType().name().toUpperCase() + "_OPERATIONS.html");
    }

    public static void sense(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new Sense(), false));
    }

    public static void remoteCancel(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.RemoteCancel(), true));
    }

    public static void setDepositMode(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetDepositMode(), true));
    }

    public static void setManualMode(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetManualMode(), true));
    }

    public static void setErrorRecoveryMode(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetErrorRecoveryMode(), true));
    }

    public static void setStroringErrorRecoveryMode(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetStroringErrorRecoveryMode(), true));
    }

    public static void setCollectMode(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetCollectMode(), true));
    }

    public static void openEscrow(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.OpenEscrow(), true));
    }

    public static void closeEscrow(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.CloseEscrow(), true));
    }

    public static void storingStart(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.StoringStart(0), true));
    }

    public static void stopCounting(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.StopCounting(), true));
    }

    public static void resetDevice(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.ResetDevice(), true));
    }

    public static void switchCurrency(Integer deviceId, Long cu) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SwitchCurrency(cu.byteValue()), true));
    }

    public static void batchDataTransmition(Integer deviceId) {
        int[] bills = new int[32];
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        bills[ 27] = 0;
        bills[ 26] = 0;
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.BatchDataTransmition(bills), true));
    }

    public static void countingDataRequest(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.CountingDataRequest(), true));
    }

    public static void amountRequest(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.AmountRequest(), true));
    }

    public static void denominationDataRequest(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.DenominationDataRequest(), true));
    }

    public static void settingDataRequestEscrow(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("ESCROW_SET"), true));
    }

    public static void settingDataRequestCassete(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("CASSETE_SET"), true));
    }

    public static void settingDataRequestReject(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("REJECT_SET"), true));
    }

    public static void logDataRequest(Integer deviceId) throws IOException, InterruptedException {
        GloryDE50TaskOperation st = glory.sendGloryDE50Operation(new devices.glory.operation.StartUpload(StartUpload.Files.COUNTER_INFO), true);
        if (st.isError()) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        int fileSize = st.getResponse().getFileSize();
        Logger.debug("Filesize : %d", fileSize);
        int block;
        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter("/tmp/test.txt");
            out = new BufferedWriter(fstream);
            for (block = 0; (block * 512) < fileSize; block++) {
                LogDataRequest l = new devices.glory.operation.LogDataRequest(block);
                st = glory.sendGloryDE50Operation(l, true);
                if (st.isError()) {
                    setStatusAndRedirect(deviceId, st);
                    return;
                }
                byte[] b = st.getResponse().getData();
                int i;
                for (i = 0; i < (b.length - 8) / 2 && (block * 512 + i) < fileSize; i++) {
                    char ch = (char) (16 * (b[2 * i + 8] - 0x30) + ((b[ 2 * i + 9] - 0x30)));
                    out.write(ch);
                }

                Logger.debug("-----> READED   %d", block * 512 + i);
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (fstream != null) {
                fstream.close();
            }
        }

        EndUpload c1 = new devices.glory.operation.EndUpload();
        st = glory.sendGloryDE50Operation(c1, true);
        if (st.isError()) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        /*Logger.debug("Request data readed : %d", fileSize);
         StringBuilder h = new StringBuilder("Readed ");
         for (byte x : data) {
         h.append(String.format("0x%x ", x));
         }
         Logger.debug(h.toString());
         st.setMsg(String.format("Readed %d bytes : %s", fileSize, h.toString()));*/
        setStatusAndRedirect(deviceId, st);
    }
    /*
     * If you want to update ‘Device Setting Data’, write data to a file by the
     * following format and download it. After download, use the command ’Device
     * Setting Data Load’. Format : Name=Data,¥r¥n Example CASSETTE_SET=5000,
     * ESCROW_SET=100,
     * REJECT_SET=1111111111111111111111111111111110000000000000000000000000000000,
     */

    public static void deviceSettingDataLoad(Integer deviceId) {
        //String s = "ESCROW_SET=100,\r\nREJECT_SET=1001111111111111111111111111111110000000000000000000000000000000,\r\n";
        String s = "ESCROW_SET=100,\r\nREJECT_SET=0000000000000000000000000000000000000000000000000000000000000000,\r\n";
        GloryDE50TaskOperation st = UploadData(s.length(), "settings.txt", s.getBytes());
        if (st != null) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.DeviceSettingDataLoad("settings.txt"), true));
    }

    public static void programUpdate(Integer deviceId) {
        // For templates C*******.DLF, for firmware A0******.MOT, for font A1******.DLF
        String gFileName = "CUPGRADE.DLF";
        //String filename = "/home/adji/Desktop/work/laguardiana/permaquim/last_sep_07_2012/DE-50/A0v0196.mot";
        String filename = "/tmp/test.txt";
        File f = new File(filename);
        byte b[] = new byte[(int) f.length()];
        try {
            FileInputStream fis = new FileInputStream(f);
            int readed = fis.read(b);
            GloryDE50TaskOperation st = UploadData(readed, gFileName, b);
            if (st != null) {
                setStatusAndRedirect(deviceId, st);
                return;
            }
        } catch (IOException e) {
            renderArgs.put("error", "Reading file");
            setStatusAndRedirect(deviceId, null);
        }
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.ProgramUpdate(gFileName), true));
    }

    public static void getFileInformation(Integer deviceId) {
        String gFileName = "UPGRADES.TXT";
        GloryDE50TaskOperation op = glory.sendGloryDE50Operation(new devices.glory.operation.GetFileInformation(gFileName), true);
        if (op.getResponse().getFileSize() > 0 && op.getResponse().getDate() != null) {
            Logger.debug("Filesize : %d, Date : %s", op.getResponse().getFileSize(), op.getResponse().getDate().toString());
        } else {
            Logger.debug("File not");
        }
        setStatusAndRedirect(deviceId, op);
    }

    public static void setTime(Integer deviceId) {
        Date now = new Date();
        //Calendar calendar = new GregorianCalendar(2007, Calendar.JANUARY, 1);
        //GregorianCalendar.getInstance().getTime()
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new devices.glory.operation.SetTime(now), true));
    }

    static private GloryDE50TaskOperation UploadData(int fileSize, String fileName, byte[] data) {
        GloryDE50TaskOperation st = glory.sendGloryDE50Operation(new devices.glory.operation.StartDownload(fileSize, fileName), true);
        if (st.isError()) {
            return st;
        }

        byte[] b = new byte[512];
        for (int j = 0; j < ((fileSize + 512) / 512); j++) {
            // TODO: Optimize using System.arraycopy.
            for (int i = 0; i < 512; i++) {
                if (j * 512 + i < data.length) {
                    b[ i] = data[ j * 512 + i];
                } else {
                    b[ i] = 0;
                }
            }
            Logger.debug("Packet no %d", j);
            st = glory.sendGloryDE50Operation(new devices.glory.operation.RequestDownload(j, b), true);
            if (st.isError()) {
                return st;
            }
        }
        st = glory.sendGloryDE50Operation(new devices.glory.operation.EndDownload(), true);
        if (st.isError()) {
            return st;
        }
        return null;
    }
//    static private GloryReturnParser DownloadData(String fileName) {
//
//        GloryReturnParser st = new GloryReturnParser(glory.sendCommand(new devices.glory.command.StartUpload(fileName), true));
//        if (st.isError()) {
//            st.setMsg("Error in StartDownload");
//            return st;
//        }
//
//        // TODO: ???
//        long quantity = c.getLongVal();
//        byte[] data;
//        try {
//            data = glory.getBytes((int) quantity);
//        } catch (IOException e) {
//            st.setMsg("Error reading data from device");
//            return st;
//        }
//
//        EndUpload c1 = new devices.glory.command.EndUpload();
//        st = new GloryReturnParser(glory.sendCommand(c, true));
//        if (st.isError()) {
//            st.setMsg("Error in EndDownload");
//            return st;
//        }
//        st.setMsg(String.format("Readed %d bytes", data.length));
//        return st;
//    }
}
