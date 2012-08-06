package controllers;

import java.util.List;
import models.Deposit;
import models.TemplatePrinter;
import models.lov.DepositUserCodeReference;
import models.db.LgBatch;
import models.db.LgLov;
import models.db.LgUser;
import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Controller;
import play.mvc.With;

@With( Secure.class )
public class Application extends Controller {

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
        TemplatePrinter.printTemplate( "<h1>My First Heading</h1><p>My first paragraph.</p>" );
        redirect( "Application.index" );
    }

    public static void inputReference() {
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        render( referenceCodes );
        // render();
    }

    public static void countMoney( String reference1, String reference2 ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        Integer ref1 = Integer.parseInt(reference1);
        LgLov userCode = DepositUserCodeReference.findByNumericId( ref1 );
        if ( userCode == null ) {
            Logger.error( "countMoney: no reference received! for %s", reference1 );
            index();
            return;
        }

        Deposit deposit = new Deposit( user, reference2, userCode );
        deposit.save();

        LgBatch batch = LgBatch.MakeRandom( deposit );
        String randomID = Codec.UUID();
        //pass gathered data and let user chose what to do..
        Cache.set( randomID + "-deposit", deposit, "60mn" );
        Cache.set( randomID + "-batch", batch, "60mn" );
        appendBatch(randomID);
    }

    public static void appendBatch( String randomID ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        //Logger.error("code received: %s", userCode.description);
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        LgBatch batch = Cache.get(randomID+"-batch", LgBatch.class); //LgBatch.MakeRandom( deposit );
        render( randomID, deposit, batch );
        return;
    }
    
    public static void appendBatch_orig( String randomID ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        //Logger.error("code received: %s", userCode.description);
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        LgBatch batch = LgBatch.MakeRandom( deposit );
        //pass gathered data and let user chose what to do..
        Logger.info("Executing after render()!!!!");
        Cache.set( randomID + "-deposit", deposit, "60mn" );
        Cache.set( randomID + "-batch", batch, "60mn" );
        render( randomID, deposit, batch );
    }
    

    public static void acceptBatch( String randomID ) {
        //user accepted to deposit it!
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        LgBatch batch = Cache.get( randomID + "-batch", LgBatch.class );
        batch.save();
        flash.success( "Deposit is done!" );
        //tell user deposit was done rightly
        render( randomID );
        deposit.addBatch( batch );
    }

    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = DepositUserCodeReference.findAll();
        render( referenceCodes );
    }
}
