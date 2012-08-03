package controllers;

import devices.glory.manager.Manager;
import java.io.IOException;
import java.util.Map;
import devices.CounterFactory;
import java.util.List;
import models.Bill;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

// TODO: Manage errors.
public class GloryManagerController extends Controller {

    static Manager.ControllerApi manager;
    static String error;

    @Before
    static void getManager() throws Throwable {
        if ( flash.get( "error" ) == null ) {
            manager = CounterFactory.getManager( Play.configuration.getProperty( "glory.port" ) );
            if ( manager == null ) {
                error = "Manager error opening port";
            } else {
                error = manager.getError();
            }
        }
    }

    public static void index() {
        if ( error != null ) {
            Logger.error( error );
            flash.put( "error", error );
        }
        List<Bill> billData = Bill.getCurrentCounters();
        if ( request.isAjax() ) {
            Object[] o = new Object[ 2 ];
            o[0] = error;
            o[1] = billData;
            renderJSON( o );
        }
        renderArgs.put( "billData", billData );
        render();
    }

    public static void count( Map<String, String> billTypeIds ) throws IOException {
        if ( manager != null ) {
            if ( !manager.count( Bill.getSlotArray( billTypeIds ) ) ) {
                error = "Still executing another command";
            }
        }
        index();
    }

    public static void cancelDeposit() throws IOException {
        if ( manager != null ) {
            if ( !manager.cancelDeposit() ) {
                error = "Not counting cant cancel";
            }
        }
        index();
    }

    public static void storeDeposit() throws IOException {
        if ( manager != null ) {
            int sequenceNumber = 1;
            if ( !manager.storeDeposit( sequenceNumber ) ) {
                error = "Not counting cant store";
            }
        }
        index();
    }

    public static void reset() throws IOException {
        if ( manager != null ) {
            if ( !manager.reset() ) {
                error = "Executing another command";
            }
        }
        index();
    }

    public static void storingErrorReset() throws IOException {
        if ( manager != null ) {

            if ( !manager.storingErrorReset() ) {
                error = "Executing another command";
            }
        }
        index();
    }
}
