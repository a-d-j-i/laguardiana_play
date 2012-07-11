package controllers;

import models.TemplatePrinter;
import play.mvc.Controller;
import play.mvc.With;

@With( SecureController.class )
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void goCounter() {
        Counter.index();
    }

    public static void goDeposit() {
        DepositReference.index();
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate( "<h1>My First Heading</h1><p>My first paragraph.</p>" );
        redirect( "Application.index" );
    }
}
