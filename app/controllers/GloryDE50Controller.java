package controllers;

import devices.glory.operation.OperationWithAckResponse;
import devices.glory.operation.EndUpload;
import devices.glory.operation.GetFileInformation;
import devices.glory.operation.LogDataRequest;
import devices.glory.operation.StartUpload;
import devices.glory.operation.Sense;
import devices.DeviceInterface;
import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50OperationResponse;
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

    private static void setStatusAndRedirect(Integer deviceId, GloryDE50OperationResponse st) {
        if (st != null) {
            Logger.debug("STATUS : %s", st.toString());
            renderArgs.put("status", st.getRepr());
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("deviceId", deviceId);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getName().toUpperCase() + "_OPERATIONS.html");
    }

    public static void sense(Integer deviceId) {
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(new Sense()));
    }

    public static void remoteCancel(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.RemoteCancel();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void setDepositMode(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SetDepositMode();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void setManualMode(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SetManualMode();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void setErrorRecoveryMode(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SetErrorRecoveryMode();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void setStroringErrorRecoveryMode(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SetStroringErrorRecoveryMode();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void setCollectMode(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SetCollectMode();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void openEscrow(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.OpenEscrow();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void closeEscrow(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.CloseEscrow();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void storingStart(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.StoringStart(0);
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void stopCounting(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.StopCounting();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void resetDevice(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.ResetDevice();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void switchCurrency(Integer deviceId, Long cu) {
        OperationWithAckResponse c = new devices.glory.operation.SwitchCurrency(cu.byteValue());
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void batchDataTransmition(Integer deviceId) {
        int[] bills = new int[32];
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        bills[ 27] = 0;
        bills[ 26] = 0;

        OperationWithAckResponse c = new devices.glory.operation.BatchDataTransmition(bills);
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void countingDataRequest(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.CountingDataRequest();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void amountRequest(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.AmountRequest();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void denominationDataRequest(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.DenominationDataRequest();
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void settingDataRequestEscrow(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SettingDataRequest("ESCROW_SET");
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void settingDataRequestCassete(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SettingDataRequest("CASSETE_SET");
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void settingDataRequestReject(Integer deviceId) {
        OperationWithAckResponse c = new devices.glory.operation.SettingDataRequest("REJECT_SET");
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void logDataRequest(Integer deviceId) throws IOException, InterruptedException {
        StartUpload c = new devices.glory.operation.StartUpload(StartUpload.Files.COUNTER_INFO);
        GloryDE50OperationResponse st = glory.sendGloryDE50Operation(c, true);
        if (st.isError()) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        int fileSize = c.getFileSize();
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
                byte[] b = st.getData();
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
        GloryDE50OperationResponse st = UploadData(s.length(), "settings.txt", s.getBytes());
        if (st != null) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        OperationWithAckResponse c = new devices.glory.operation.DeviceSettingDataLoad("settings.txt");
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
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
            GloryDE50OperationResponse st = UploadData(readed, gFileName, b);
            if (st != null) {
                setStatusAndRedirect(deviceId, st);
                return;
            }
        } catch (Exception e) {
            renderArgs.put("error", "Reading file");
            setStatusAndRedirect(deviceId, null);
        }

        OperationWithAckResponse c = new devices.glory.operation.ProgramUpdate(gFileName);
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    public static void getFileInformation(Integer deviceId) {
        String gFileName = "UPGRADES.TXT";
        GetFileInformation c = new devices.glory.operation.GetFileInformation(gFileName);
        GloryDE50OperationResponse response = glory.sendGloryDE50Operation(c, true);
        if (c.getFileSize() > 0 && c.getDate() != null) {
            Logger.debug("Filesize : %d, Date : %s", c.getFileSize(), c.getDate().toString());
        } else {
            Logger.debug("File not");
        }
        setStatusAndRedirect(deviceId, response);
    }

    public static void setTime(Integer deviceId) {
        Date now = new Date();
        //Calendar calendar = new GregorianCalendar(2007, Calendar.JANUARY, 1);
        //GregorianCalendar.getInstance().getTime()
        OperationWithAckResponse c = new devices.glory.operation.SetTime(now);
        setStatusAndRedirect(deviceId, glory.sendGloryDE50Operation(c, true));
    }

    static private GloryDE50OperationResponse UploadData(int fileSize, String fileName, byte[] data) {
        OperationWithAckResponse c = new devices.glory.operation.StartDownload(fileSize, fileName);
        GloryDE50OperationResponse st = glory.sendGloryDE50Operation(c, true);
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
            c = new devices.glory.operation.RequestDownload(j, b);
            st = glory.sendGloryDE50Operation(c, true);
            if (st.isError()) {
                return st;
            }
        }
        c = new devices.glory.operation.EndDownload();
        st = glory.sendGloryDE50Operation(c, true);
        if (st.isError()) {
            return st;
        }
        return null;
    }
//    static private GloryReturnParser DownloadData(String fileName) {
//
//        CommandWithFileLongResponse c = new devices.glory.command.StartUpload(fileName);
//        GloryReturnParser st = new GloryReturnParser(glory.sendCommand(c, true));
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