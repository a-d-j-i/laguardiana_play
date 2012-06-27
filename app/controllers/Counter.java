package controllers;

import play.mvc.*;




@With(Secure.class)
public class Counter extends Controller {

    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            //LgUser user = User.find("byEmail", Security.connected()).first();
            //renderArgs.put("user", user.fullname);
        }
    }
    
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
