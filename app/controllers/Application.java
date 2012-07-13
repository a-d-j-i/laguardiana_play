package controllers;
import java.util.List;

import play.mvc.Controller;
import play.mvc.With;

import models.TemplatePrinter;
import models.LgLov;


@With( SecureController.class )
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void goCounter() {
        Counter.index();
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate( "<h1>My First Heading</h1><p>My first paragraph.</p>" );
        redirect( "Application.index" );
    }
    
    public static void inputReference() {
        //temporarily until we have a page using getReferences()..
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();     
        render(referenceCodes);
        // render();
    }
    
    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();     
        render(referenceCodes);
    }
}
