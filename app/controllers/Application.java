package controllers;

import java.util.List;
import models.TemplatePrinter;
import models.db.LgLov;
import models.lov.DepositUserCodeReference;
import play.mvc.Controller;
import play.mvc.With;

@With( Secure.class)
public class Application extends BaseController {

    public static void index() {
        render();
    }

    public static void bootstrap() {
        render();
    }

    public static void goCounter() {
        Counter.index();
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate("<h1>My First Heading</h1><p>My first paragraph.</p>");
        redirect("Application.index");
    }

    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = DepositUserCodeReference.findAll();
        render(referenceCodes);
    }
}
