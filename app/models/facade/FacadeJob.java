package models.facade;

import static bootstrap.AppStart.singleThreadExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import play.Logger;
import play.jobs.Job;
import play.libs.F;

/**
 * Make all the access to the facade sequencial.
 * @author adji
 * @param <V>
 */
public class FacadeJob<V> extends Job<V> {

    @Override
    public F.Promise<V> now() {
        final F.Promise<V> smartFuture = new F.Promise<V>();
        singleThreadExecutor.submit(new Callable<V>() {
            public V call() throws Exception {
                V result = FacadeJob.this.call();
                smartFuture.invoke(result);
                return result;
            }
        });
        return smartFuture;
    }

    public V runNow() {
        try {
            return now().get();
        } catch (InterruptedException ex) {
            Logger.error("Excetion inm runNow : %s", ex);
        } catch (ExecutionException ex) {
            Logger.error("Excetion inm runNow : %s", ex);
            ex.printStackTrace();
        }
        return null;
    }
}
