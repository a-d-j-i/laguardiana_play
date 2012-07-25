
import devices.CounterFactory;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

@OnApplicationStop
public class TearDown extends Job {

    @Override
    public void doJob() {
        CounterFactory.closeAll();
    }
}
