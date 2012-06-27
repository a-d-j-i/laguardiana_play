package controllers;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;



@With(Secure.class)
public class Application extends Controller {

    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            /*LgUser user = LgUser.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user.fullname);*/
        }
    }
    
    public static void index() {
        //System.out.println("Yáop");
        render();
    }

    public static void goCounter() {
        System.out.println("go to counter");
        Counter.index();
    }
}
