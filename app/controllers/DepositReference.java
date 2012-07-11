package controllers;

import play.mvc.*;
import java.util.List;
import models.LgLov;

@With( SecureController.class )
public class DepositReference extends Controller {

    public static void index() {
//        User user = User.find("byEmail", Security.connected()).first();

//        CountList cl = new CountList(user).save();
//        cl.addFromList(interfaces.FakeGlory.retrieveCount());

        //renderArgs.put("countResult", cl.value());
        //System.out.println("vlaue:"+cl.value());
        //render(cl);
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();     
        render(referenceCodes);
    }
}
