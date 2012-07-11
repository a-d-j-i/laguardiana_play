package controllers;

import play.mvc.Controller;
import play.mvc.With;

@With( SecureController.class )
public class Application extends Controller {

    public static void index() {
        //TemplatePrinter.printTemplate( "printerOutput" );
        render();
    }

    public static void goCounter() {
        Counter.index();
    }

    public static void goDeposit() {
        DepositReference.index();
    }
}
