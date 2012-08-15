package controllers;

import play.mvc.Controller;
import play.mvc.With;

// TODO: manage the bag 
@With( Secure.class)
public class BagController extends Controller {

    // Show a list of deposits in this bag.
    // And print it if possible.
    public static void withdraw() {
        render();
    }

    public static void withdrawDone() {
        render();
    }

}
