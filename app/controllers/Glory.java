package controllers;

import com.google.gson.Gson;
import models.GloryService;
import devices.glory.GloryStatus;
import java.io.IOException;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Router;

public class Glory extends Controller {

    static GloryService gloryService;

    public static void index() {
        GloryStatus lastStatus = null;
        String lst = flash.get( "lastStatus" );
        if ( lst != null ) {
            if ( !lst.equalsIgnoreCase( "null" ) ) {
                Gson gson = new Gson();
                lastStatus = gson.fromJson( lst, GloryStatus.class );
            }
        }
        render( lastStatus );
    }

    private static void setStatusAndRedirect( GloryStatus st ) {
        if ( st == null ) {
            flash.put( "lastStatus", null );
        } else {
            if ( st.getError() != null ) {
                flash.put( "error", st.getError() );
            } else {
                flash.put( "success", "success" );
            }
            Gson gson = new Gson();
            String json = gson.toJson( st );
            flash.put( "lastStatus", json );
        }
        redirect( Router.reverse( "Glory.index" ).url );
    }

    public static void sense() throws IOException {
        setStatusAndRedirect( GloryService.sense() );
    }

    public static void remoteCancel() {
        setStatusAndRedirect( GloryService.remoteCancel() );
    }

    public static void setDepositMode() {
        setStatusAndRedirect( GloryService.setDepositMode() );
    }

    public static void setManualMode() {
        setStatusAndRedirect( GloryService.setManualMode() );
    }

    public static void setErrorRecoveryMode() {
        setStatusAndRedirect( GloryService.setErrorRecoveryMode() );
    }

    public static void setStroringErrorRecoveryMode() {
        setStatusAndRedirect( GloryService.setStroringErrorRecoveryMode() );
    }

    public static void openEscrow() {
        setStatusAndRedirect( GloryService.openEscrow() );
    }

    public static void closeEscrow() {
        setStatusAndRedirect( GloryService.closeEscrow() );
    }

    public static void storingStart() {
        setStatusAndRedirect( GloryService.StroingStart() );
    }

    public static void stopCounting() {
        setStatusAndRedirect( GloryService.StopCounting() );
    }

    public static void resetDevice() {
        setStatusAndRedirect( GloryService.ResetDevice() );
    }

    public static void switchCurrency( Long c ) {
        setStatusAndRedirect( GloryService.SwitchCurrency( c.byteValue() ) );
    }

    public static void batchDataTransmition() {
        int[] bills = new int[ 32 ];
        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        bills[ 25] = 1;
        setStatusAndRedirect( GloryService.BatchDataTransmition( bills ) );
    }

    public static void countingDataRequest() throws IOException {
        setStatusAndRedirect( GloryService.CountingDataRequest() );
    }

    public static void amountRequest() {
        setStatusAndRedirect( GloryService.AmountRequest() );
    }

    public static void denominationDataRequest() throws IOException {
        setStatusAndRedirect( GloryService.DenominationDataRequest() );
    }

    public static void settingDataRequest() throws IOException {
        setStatusAndRedirect( GloryService.SettingDataRequest() );
    }

    public static void logDataRequest() throws IOException {
        setStatusAndRedirect( GloryService.LogDataRequest() );
    }

    public static void deviceSettingDataLoad() {
        setStatusAndRedirect( GloryService.DeviceSettingDataLoad() );
    }

    public static void programUpdate() {
        setStatusAndRedirect( GloryService.ProgramUpdate() );
    }

    public static void setTime() {
        setStatusAndRedirect( GloryService.SetTime() );
    }

    public static void downloadData() {
        setStatusAndRedirect( GloryService.DownloadData( "settings.txt" ) );
    }
}
