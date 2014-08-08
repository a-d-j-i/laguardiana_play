/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        singleThreadExecutor.shutdown();
        try {
            singleThreadExecutor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
        singleThreadExecutor.shutdownNow();
    }
}
