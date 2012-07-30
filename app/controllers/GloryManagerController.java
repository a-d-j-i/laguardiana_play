package controllers;

import com.google.gson.Gson;
import devices.CounterFactory;
import devices.glory.GloryReturnParser;
import java.io.IOException;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

public class GloryManagerController extends Controller {

    static devices.glory.manager.Manager manager;

    @Before
    static void getManager() throws Throwable {
        if ( flash.get( "error" ) == null ) {
            manager = CounterFactory.getManager( Play.configuration.getProperty( "glory.port" ) );
            if ( manager.getError() != null ) {
                Logger.error( String.format( "Manager error %s", manager.getError() ) );
                flash.put( "error", manager.getError() );
                if ( Play.mode == Play.Mode.DEV ) {
                    manager.startDeposit();
                }
                redirect( Router.reverse( "GloryManagerController.index" ).url );
            }
        }
    }

    public static void index() {
        render();
    }

    public static void billDeposit() throws IOException {
        manager.startDeposit();
        index();
    }
}
