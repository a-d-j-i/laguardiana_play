/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import static devices.DeviceFactory.eventExecutor;
import java.util.concurrent.Callable;
import play.jobs.Job;
import play.libs.F;

/**
 *
 * @author adji
 */
public class DeviceFactoryEventJob<V> extends Job<V> {

    @Override
    public F.Promise<V> now() {
        final F.Promise<V> smartFuture = new F.Promise<V>();
        eventExecutor.submit(new Callable<V>() {
            public V call() throws Exception {
                V result = DeviceFactoryEventJob.this.call();
                smartFuture.invoke(result);
                return result;
            }
        });
        return smartFuture;
    }
}
