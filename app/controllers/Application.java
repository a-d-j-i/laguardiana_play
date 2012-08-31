package controllers;

import models.TemplatePrinter;
import play.mvc.With;

@With(Secure.class)
public class Application extends BaseController {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    public static void otherMenu() {
        render();
    }

    public static void bootstrap() {
        render();
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate("<h1>My First Heading</h1><p>My first paragraph.</p>");
        redirect("Application.index");
    }
}
