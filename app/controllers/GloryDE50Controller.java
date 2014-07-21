package controllers;

import com.google.gson.Gson;
import devices.device.DeviceEvent;
import devices.glory.operation.EndUpload;
import devices.glory.operation.LogDataRequest;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.operation.Sense;
import devices.glory.operation.StartUpload;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.task.GloryDE50TaskOperation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import machines.MachineDeviceDecorator;
import models.db.LgDevice;
import play.Logger;
import play.mvc.Before;
import play.mvc.Util;

public class GloryDE50Controller extends Application {

    static MachineDeviceDecorator glory;

    @Before
    static void getCounter(Integer deviceId) throws Throwable {
        DeviceController.getCounter(deviceId);
        if (DeviceController.device.getType() == LgDevice.DeviceType.GLORY_DE50) {
            glory = DeviceController.device;
        } else {
            renderArgs.put("error", "invalid device id");
            getStatus(deviceId, true);
        }
    }

    // Counter Class end
    public static void getStatus(Integer deviceId, boolean retval) {
        GloryDE50OperationResponse response = new Gson().fromJson(flash.get("status"), GloryDE50OperationResponse.class);
        if (response != null) {
            Logger.debug("STATUS : %s", response.toString());
            renderArgs.put("status", response.getRepr());
        }
        renderArgs.put("lastCmd", flash.get("lastCmd"));
        renderArgs.put("lastResult", retval ? "SUCCESS" : "FAIL");
        DeviceEvent de = glory.getLastEvent();
        String lastEvent = "";
        if (de != null) {
            lastEvent = de.toString();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[1];
            ret[ 0] = lastEvent;
            renderJSON(ret);
        } else {
            renderArgs.put("deviceId", deviceId);
            renderArgs.put("device", glory);
            renderArgs.put("lastEvent", lastEvent);
            render("DeviceController/" + glory.getType().name().toUpperCase() + "_OPERATIONS.html");
        }
    }

    public static void sense(Integer deviceId) {
        flash.put("lastCmd", "sense");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new Sense()));
    }

    public static void remoteCancel(Integer deviceId) {
        flash.put("lastCmd", "remoteCancel");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.RemoteCancel()));
    }

    public static void setDepositMode(Integer deviceId) {
        flash.put("lastCmd", "setDepositMode");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetDepositMode()));
    }

    public static void setManualMode(Integer deviceId) {
        flash.put("lastCmd", "setManualMode");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetManualMode()));
    }

    public static void setErrorRecoveryMode(Integer deviceId) {
        flash.put("lastCmd", "setErrorRecoveryMode");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetErrorRecoveryMode()));
    }

    public static void setStroringErrorRecoveryMode(Integer deviceId) {
        flash.put("lastCmd", "setStroringErrorRecoveryMode");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetStroringErrorRecoveryMode()));
    }

    public static void setCollectMode(Integer deviceId) {
        flash.put("lastCmd", "setCollectMode");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetCollectMode()));
    }

    public static void openEscrow(Integer deviceId) {
        flash.put("lastCmd", "openEscrow");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.OpenEscrow()));
    }

    public static void closeEscrow(Integer deviceId) {
        flash.put("lastCmd", "closeEscrow");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.CloseEscrow()));
    }

    public static void storingStart(Integer deviceId) {
        flash.put("lastCmd", "storingStart");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.StoringStart(0)));
    }

    public static void stopCounting(Integer deviceId) {
        flash.put("lastCmd", "stopCounting");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.StopCounting()));
    }

    public static void resetDevice(Integer deviceId) {
        flash.put("lastCmd", "resetDevice");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.ResetDevice()));
    }

    public static void switchCurrency(Integer deviceId, Long cu) {
        flash.put("lastCmd", "switchCurrency");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SwitchCurrency(cu.byteValue())));
    }

    public static void batchDataTransmition(Integer deviceId) {
        flash.put("lastCmd", "batchDataTransmition");
        int[] bills = new int[32];
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        bills[ 27] = 0;
        bills[ 26] = 0;
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.BatchDataTransmition(bills)));
    }

    public static void countingDataRequest(Integer deviceId) {
        flash.put("lastCmd", "countingDataRequest");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.CountingDataRequest()));
    }

    public static void amountRequest(Integer deviceId) {
        flash.put("lastCmd", "amountRequest");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.AmountRequest()));
    }

    public static void denominationDataRequest(Integer deviceId) {
        flash.put("lastCmd", "denominationDataRequest");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.DenominationDataRequest()));
    }

    public static void settingDataRequestEscrow(Integer deviceId) {
        flash.put("lastCmd", "settingDataRequestEscrow");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("ESCROW_SET")));
    }

    public static void settingDataRequestCassete(Integer deviceId) {
        flash.put("lastCmd", "settingDataRequestCassete");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("CASSETE_SET")));
    }

    public static void settingDataRequestReject(Integer deviceId) {
        flash.put("lastCmd", "settingDataRequestReject");
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SettingDataRequest("REJECT_SET")));
    }

    public static void logDataRequest(Integer deviceId) throws IOException, InterruptedException {
        flash.put("lastCmd", "logDataRequest");
        GloryDE50TaskOperation st = sendGloryDE50Operation(new devices.glory.operation.StartUpload(StartUpload.Files.COUNTER_INFO));
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
                st = sendGloryDE50Operation(l);
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
        st = sendGloryDE50Operation(c1);
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
        flash.put("lastCmd", "deviceSettingDataLoad");
        //String s = "ESCROW_SET=100,\r\nREJECT_SET=1001111111111111111111111111111110000000000000000000000000000000,\r\n";
        String s = "ESCROW_SET=100,\r\nREJECT_SET=0000000000000000000000000000000000000000000000000000000000000000,\r\n";
        GloryDE50TaskOperation st = UploadData(s.length(), "settings.txt", s.getBytes());
        if (st != null) {
            setStatusAndRedirect(deviceId, st);
            return;
        }
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.DeviceSettingDataLoad("settings.txt")));
    }

    public static void programUpdate(Integer deviceId) {
        flash.put("lastCmd", "programUpdate");
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
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.ProgramUpdate(gFileName)));
    }

    public static void getFileInformation(Integer deviceId) {
        flash.put("lastCmd", "getFileInformation");
        String gFileName = "UPGRADES.TXT";
        GloryDE50TaskOperation op = sendGloryDE50Operation(new devices.glory.operation.GetFileInformation(gFileName));
        if (op.getResponse().getFileSize() > 0 && op.getResponse().getDate() != null) {
            Logger.debug("Filesize : %d, Date : %s", op.getResponse().getFileSize(), op.getResponse().getDate().toString());
        } else {
            Logger.debug("File not");
        }
        setStatusAndRedirect(deviceId, op);
    }

    public static void setTime(Integer deviceId) {
        flash.put("lastCmd", "setTime");
        Date now = new Date();
        //Calendar calendar = new GregorianCalendar(2007, Calendar.JANUARY, 1);
        //GregorianCalendar.getInstance().getTime()
        setStatusAndRedirect(deviceId, sendGloryDE50Operation(new devices.glory.operation.SetTime(now)));
    }

    static private GloryDE50TaskOperation UploadData(int fileSize, String fileName, byte[] data) {
        flash.put("lastCmd", "UploadData");
        GloryDE50TaskOperation st = sendGloryDE50Operation(new devices.glory.operation.StartDownload(fileSize, fileName));
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
            st = sendGloryDE50Operation(new devices.glory.operation.RequestDownload(j, b));
            if (st.isError()) {
                return st;
            }
        }
        st = sendGloryDE50Operation(new devices.glory.operation.EndDownload());
        if (st.isError()) {
            return st;
        }
        return null;
    }
//    static private GloryReturnParser DownloadData(String fileName) {
//
//        GloryReturnParser st = new GloryReturnParser(glory.sendCommand(new devices.glory.command.StartUpload(fileName)));
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
//        st = new GloryReturnParser(glory.sendCommand(c));
//        if (st.isError()) {
//            st.setMsg("Error in EndDownload");
//            return st;
//        }
//        st.setMsg(String.format("Readed %d bytes", data.length));
//        return st;
//    }

    @Util
    static public GloryDE50TaskOperation sendGloryDE50Operation(OperationWithAckResponse c) {
        try {
            GloryDE50TaskOperation deviceTask = new GloryDE50TaskOperation(c, true);
            glory.submit(deviceTask).get();
            return deviceTask;
        } catch (InterruptedException ex) {
            Logger.error("exeption in sendGloryDE50Operation %s", ex);
        } catch (ExecutionException ex) {
            Logger.error("exeption in sendGloryDE50Operation %s", ex);
        }
        return null;
    }

    @Util
    private static void setStatusAndRedirect(Integer deviceId, GloryDE50TaskOperation op) {
        // TODO: op.isError();
        if (op.getResponse() != null) {
            flash.put("status", new Gson().toJson(op.getResponse()));
        }
        getStatus(deviceId, !op.isError());
    }

}
