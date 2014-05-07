/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50OperationResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StateOperation extends GloryDE50StateAbstract {

    final SynchronousQueue<Callable<GloryDE50StateAbstract>> syncQueue = new SynchronousQueue<Callable<GloryDE50StateAbstract>>();

    public GloryDE50StateOperation(GloryDE50Device.GloryDE50StateMachineApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract step() {
        try {
            Callable<GloryDE50StateAbstract> c = syncQueue.poll(1000, TimeUnit.MILLISECONDS);
            if (c != null) {
                return c.call();
            }
        } catch (InterruptedException ex) {
        } catch (Exception ex) {
            Logger.debug("GloryDE50WaitForOperation exception : %s", ex.toString());
        }
        return this;
    }

    synchronized protected boolean comunicate(Callable<GloryDE50StateAbstract> callable) {
        return syncQueue.offer(callable);
    }

    @Override
    public boolean sendOperation(final FutureTask<GloryDE50OperationResponse> t) {
        return comunicate(new Callable<GloryDE50StateAbstract>() {
            public GloryDE50StateAbstract call() throws Exception {
                t.run();
                return null;
            }
        });

    }

}
