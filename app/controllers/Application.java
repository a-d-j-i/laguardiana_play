package controllers;
import java.util.List;

import play.mvc.Controller;
import play.mvc.With;

import play.libs.*;
import play.cache.*;

import models.*;

import play.cache.Cache;
import play.Logger;

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
    
    public static void countMoney(String reference1, String reference2) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );        
        LgLov userCode = LgLov.FromUserCodeReference(reference1);
        if (userCode == null) {
            Logger.error("countMoney: no reference received!");
            index();
            return;
        }
        //Logger.error("code received: %s", userCode.description);
        LgDeposit deposit = new LgDeposit(user, reference2, userCode);
        deposit.save();
        String randomID = Codec.UUID();
        render(randomID, deposit);
    }
    
    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();     
        render(referenceCodes);
    }
}
