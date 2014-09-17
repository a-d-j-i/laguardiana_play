package bootstrap;

import java.util.concurrent.TimeUnit;
import machines.jobs.MachineJob;
import models.ModelFacade;
import play.Logger;
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
        Logger.debug("AppStop start");
        ModelFacade.stop();
        Logger.debug("AppStop facade stop");
        Logger.debug("AppStop executor shutdown");
        // Ignore pending jobs.
        MachineJob.singleThreadExecutor.shutdownNow();
        try {
            Logger.debug("AppStop executor shutdown wait");
            MachineJob.singleThreadExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.debug("AppStop exception waiting for executor end %s", ex.toString());
        }
        Logger.debug("AppStop done");
    }
}
