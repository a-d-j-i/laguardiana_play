package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import models.*;


@With(Secure.class)
public class Application extends Controller {

    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user.fullname);
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
