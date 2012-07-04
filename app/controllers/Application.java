package controllers;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;



@With(SecureController.class)
public class Application extends Controller {

   
    public static void index() {
        //System.out.println("Yáop");
        render();
    }

    public static void goCounter() {
        System.out.println("go to counter");
        Counter.index();
    }
}
