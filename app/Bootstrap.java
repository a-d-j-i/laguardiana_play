
import models.LgUser;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        // Check if the database is empty
        if ( LgUser.count() == 0 ) {
            Logger.info( "loading initial-data.yml as no users were found!" );
            Fixtures.loadModels( "lov-data.yml" );
            Fixtures.loadModels( "initial-data.yml" );
        };
    }
}
