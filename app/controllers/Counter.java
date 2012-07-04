package controllers;

import play.mvc.*;

@With( SecureController.class )
public class Counter extends Controller {

    public static void index() {
//        User user = User.find("byEmail", Security.connected()).first();

//        CountList cl = new CountList(user).save();
//        cl.addFromList(interfaces.FakeGlory.retrieveCount());

        //renderArgs.put("countResult", cl.value());
        //System.out.println("vlaue:"+cl.value());
        //render(cl);
        render();
    }
}
