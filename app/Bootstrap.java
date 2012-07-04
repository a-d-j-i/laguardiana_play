import play.jobs.*;
import org.junit.*;
import play.test.*;
import models.*;
 
 
@OnApplicationStart
public class Bootstrap extends Job {
 
    public void doJob() {
        // Check if the database is empty
        if(LgUser.count() == 0) {
            System.out.println("loading initial-data.yml as no users were found!");
            Fixtures.loadModels("initial-data.yml");
        };
    }
}

