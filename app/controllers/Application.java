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
        Integer saved = 0;
        Cache.set( randomID + "-saved", saved, "60mn" );
        appendBatch(randomID);
    }

    public static void appendBatch( String randomID ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        LgBatch batch = Cache.get(randomID+"-batch", LgBatch.class);
        render( randomID, deposit, batch );
    }
    
 
    public static void acceptBatch( String randomID ) {
        //user accepted to deposit it!
        Logger.info("About to restore data!!!!");
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        LgBatch batch = Cache.get( randomID + "-batch", LgBatch.class );
        Integer saved = Cache.get( randomID + "-saved", Integer.class );
        Logger.info("About to save1!!!!");
        batch.save();
        Logger.info("After save1!!!!");
        flash.success( "Deposit is done!" );
        //tell user deposit was done rightly
        LgBatch newbatch = LgBatch.MakeRandom( deposit );
        Cache.set( randomID + "-batch", newbatch, "60mn" );
        saved = 1;
        Cache.set( randomID + "-saved", saved, "60mn" );
        render( randomID );
    }

    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = DepositUserCodeReference.findAll();
        render( referenceCodes );
    }
}
