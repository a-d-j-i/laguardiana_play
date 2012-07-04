package controllers;

import models.LgUser;
import play.mvc.Before;
import play.mvc.With;
/**
 *
 * @author adji
 *
 * Base controller for all lg controllers, uses security and auth.
 */
@With( Secure.class )
@Check("value")
public class BaseController {

    @Before
    static void setConnectedUser() {
        if ( Security.isConnected() ) {
            LgUser user = LgUser.find( "byUsername", Security.connected() ).first();
            //renderArgs.put("user", user.fullname);
        }
    }
}
