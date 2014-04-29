package bootstrap;

import static bootstrap.AppStart.eventExecutor;
import java.util.concurrent.Callable;
import play.jobs.Job;
import play.libs.F;

/**
 *
 * @author adji
 */
public class BootstrapEventJob<V> extends Job<V> {

    @Override
    public F.Promise<V> now() {
        final F.Promise<V> smartFuture = new F.Promise<V>();
        eventExecutor.submit(new Callable<V>() {
            public V call() throws Exception {
                V result = BootstrapEventJob.this.call();
                smartFuture.invoke(result);
                return result;
            }
        });
        return smartFuture;
    }
}
