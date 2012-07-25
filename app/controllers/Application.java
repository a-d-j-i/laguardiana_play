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
        List<LgLov> referenceCodes = LgLov.findAll();
        render( referenceCodes );
        // render();
    }

    public static void countMoney( String reference1, String reference2 ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        LgLov userCode = DepositUserCodeReference.findByTextId( reference1 );
        if ( userCode == null ) {
            Logger.error( "countMoney: no reference received!" );
            index();
            return;
        }

        //Logger.error("code received: %s", userCode.description);
        Deposit deposit = new Deposit( user, reference2, userCode );
        deposit.save();

        LgBatch batch = LgBatch.MakeRandom();

        String randomID = Codec.UUID();
        Cache.set( randomID + "-deposit", deposit, "60mn" );
        Cache.set( randomID + "-batch", batch, "60mn" );
        render( randomID, deposit, batch );
    }

    public static void acceptBatch( String randomID ) {
        // XXX To Do Dave...
        Deposit deposit = Cache.get( randomID + "-deposit", Deposit.class );
        //deposit.save();
        LgBatch batch = Cache.get( randomID + "-batch", LgBatch.class );
        deposit.addBatch( batch );
        //batch.save();
        //deposit.save();
    }

    // rest 
    public static void getReferenceCodes() {
        List<LgLov> referenceCodes = DepositUserCodeReference.findAll();
        render( referenceCodes );
    }
}
