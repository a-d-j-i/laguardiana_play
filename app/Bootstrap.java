
import models.Deposit;
import models.db.LgLov;
import models.db.LgUser;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        // Check if the database is empty
        if ( LgLov.count() == 0 ) {
            Logger.info( "loading lov-data.yml as no data were found!" );
            Fixtures.loadModels( "lov-data.yml" );
        }
        if ( LgUser.count() == 0 ) {
            Logger.info( "loading user-data.yml as no users were found!" );
            Fixtures.loadModels( "user-data.yml" );
        }
        if ( Play.mode.isDev() ) {
            if ( Deposit.count() == 0 ) {
                Logger.info( "loading dev-data.yml!" );
                //Fixtures.loadModels( "dev-data.yml" );
            }
        }
    }
}
