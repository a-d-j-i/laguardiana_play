package bootstrap;

import static bootstrap.AppStart.singleThreadExecutor;
import java.util.concurrent.TimeUnit;
import models.ModelFacade;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

/**
 *
 * @author adji
 */
@OnApplicationStop
public class AppStop extends Job {

    @Override
    public void doJob() {
        ModelFacade.stop();
        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdown();
            try {
                singleThreadExecutor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
            }
        }
    }
}
