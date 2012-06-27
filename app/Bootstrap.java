import play.jobs.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
 
    public void doJob() {
        // Check if the database is empty
        System.out.println("Hello, World");
        /*if(User.count() == 0) {
            System.out.println("counter is 0.");
            Fixtures.loadModels("initial-data.yml");
        };*/ // else {
           // System.out.println("loading anyways..");
           // Fixtures.loadModels("initial-data.yml");
           // }
    }
 
}

