/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstrap;

import static bootstrap.AppStart.eventExecutor;
import java.util.concurrent.TimeUnit;
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
        Logger.debug("onApplicationStop close all devices");
        ModelFacade.stop();
        Logger.debug("onApplicationStop close all devices DONE");
        Logger.debug("onApplicationStop stop execution service");
        eventExecutor.shutdown();
        try {
            eventExecutor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
        eventExecutor.shutdownNow();
    }
}
