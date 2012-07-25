package controllers;

import models.CounterStatus;
import play.libs.F.Promise;
import play.mvc.Before;
import play.mvc.Controller;

public class GloryManager extends Controller {

    static CounterStatus gloryStatus;

    @Before
    static void getCounter() throws Throwable {
        gloryStatus = CounterStatus.getInstance();
    }

    public static void startCount() {
        if ( !gloryStatus.setCurrentState( CounterStatus.CurrentState.COUNT ) ) {
            render( "Glory ocupied" );
        }
        Promise<String> task1 = new jobs.Count( gloryStatus ).now();
        String val = await( task1 );
        render( val );
    }

    public static void currentAmountRequest() {
        
    }
}
