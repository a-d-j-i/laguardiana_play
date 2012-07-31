package controllers;

import devices.CounterFactory;
import java.io.IOException;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

// TODO: Manage errors.
public class GloryManagerController extends Controller {

    static devices.glory.manager.Manager manager;

    @Before
    static void getManager() throws Throwable {
        if ( flash.get( "error" ) == null ) {
            manager = CounterFactory.getManager( Play.configuration.getProperty( "glory.port" ) );
            String error;
            if ( manager == null ) {
                error = "Manager error opening port";
            } else {
                error = manager.getError();
            }
            if ( error != null ) {
                Logger.error( error );
                flash.put( "error", error );
            }
        }
    }

    public static void index() {
        render();
    }

    public static void billDeposit() throws IOException {
        if ( manager != null ) {
            boolean ret = manager.billDeposit();
        }
        index();
    }

    public static void getBillDepositData() throws IOException {
        if ( manager != null ) {
            renderJSON( manager.getBillDepositData() );
        }
    }

    public static void cancelDeposit() throws IOException {
        if ( manager != null ) {
            boolean ret = manager.cancelDeposit();
        }
        index();
    }

    public static void storeDeposit() throws IOException {
        if ( manager != null ) {
            int sequenceNumber = 1;
            boolean ret = manager.storeDeposit( sequenceNumber );
        }
        index();
    }

    public static void reset() throws IOException {
        if ( manager != null ) {
            boolean ret = manager.reset();
        }
        index();
    }
}
