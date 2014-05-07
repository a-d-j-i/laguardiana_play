/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import play.Logger;

/**
 *
 * @author adji
 */
public class OpenPort extends GloryDE50StateAbstract {

    final ArrayBlockingQueue<FutureTask<Boolean>> syncQueue = new ArrayBlockingQueue<FutureTask<Boolean>>(1);

    public OpenPort(GloryDE50StateMachineApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract step() {
        // wait for port change.
        try {
            FutureTask<Boolean> t = syncQueue.poll(1000, TimeUnit.MILLISECONDS);
            if (t != null) {
                t.run();
            }
            if (api.isPortOpen()) {
                Logger.debug("Port open success");
                return new GotoNeutral(api);
            }
            Logger.debug("Port not open, polling");
            return this;
        } catch (InterruptedException ex) {
            Logger.debug("GloryDE50WaitForOperation exception : %s", ex.toString());
        }
        return this;
    }

    @Override
    synchronized public boolean openPort(final String pvalue, boolean wait) {
        final FutureTask<Boolean> t = new FutureTask<Boolean>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return api.open(pvalue);
            }
        });
        try {
            boolean ret = syncQueue.offer(t, 300, TimeUnit.MILLISECONDS);
            if (ret && wait) {
                ret = t.get();
                return ret;
            }
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
        return false;
    }
}
