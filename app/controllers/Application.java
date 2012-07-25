package controllers;

import models.db.LgDeposit;
import models.db.LgLov;
import models.db.LgBatch;
import models.db.LgUser;
import java.util.List;

import play.mvc.Controller;
import play.mvc.With;

import play.libs.*;

import models.*;

import play.cache.Cache;
import play.Logger;

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
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();
        render( referenceCodes );
        // render();
    }

    public static void countMoney( String reference1, String reference2 ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        LgLov userCode = LgLov.FromUserCodeReference( Integer.parseInt(reference1) );
        if ( userCode == null ) {
            Logger.error( "countMoney: no reference received! for %s", reference1 );
            index();
            return;
        }

        //Logger.error("code received: %s", userCode.description);
        LgDeposit deposit = new LgDeposit( user, reference2, userCode );
        deposit.save();

        LgBatch batch = LgBatch.MakeRandom(deposit);
        //batch.save();
        String randomID = Codec.UUID();
        //pass gathered data and let user chose what to do..
        Cache.set( randomID + "-deposit", deposit, "60mn" );
        Cache.set( randomID + "-batch", batch, "60mn" );
        render( randomID, deposit, batch );
    }

    public static void appendBatch( String randomID ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );

        //Logger.error("code received: %s", userCode.description);
        LgDeposit deposit = Cache.get( randomID + "-deposit", LgDeposit.class );

        LgBatch batch = LgBatch.MakeRandom(deposit);
        //batch.save();
        //String randomID = Codec.UUID();
        //pass gathered data and let user chose what to do..
        Cache.set( randomID + "-deposit", deposit, "60mn" );
        Cache.set( randomID + "-batch", batch, "60mn" );
        render( randomID, deposit, batch );
    }

    public static void acceptBatch( String randomID ) {
        //user accepted to deposit it!
        LgDeposit deposit = Cache.get( randomID + "-deposit", LgDeposit.class );
        LgBatch batch = Cache.get( randomID + "-batch", LgBatch.class );
        batch.save();
        flash.success("Deposit is done!");
        //tell user deposit was done rightly
        //deposit.addBatch( batch );
        render(randomID);
    }

    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = LgLov.getReferenceCodes();
        render( referenceCodes );
    }
}
