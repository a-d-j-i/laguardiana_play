package controllers;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;



@With( Secure.class )
public class Counter extends BaseController {

    public static void index() {
//        User user = User.find("byEmail", Security.connected()).first();
//        CountList cl = new CountList(user).save();
//        cl.addFromList(interfaces.FakeGlory.retrieveCount());
        //renderArgs.put("countResult", cl.value());
        //System.out.println("vlaue:"+cl.value());
        //render(cl);
        String reference1 = Cache.get("reference1", String.class);
        String reference2 = Cache.get("reference2", String.class);   
        render(reference1, reference2);
    }
    
    public static void postBeginDeposit(String reference1, String reference2) {
        //Tuple<String,String> references = new Tuple(reference1, reference2);
        Cache.set("reference1", reference1, "10mn");
        Cache.set("reference2", reference2, "10mn");
        index();
    }
}
