/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
public class WaitForOperation extends GloryDE50StateOperation {

    final SynchronousQueue<Callable<GloryDE50StateAbstract>> syncQueue = new SynchronousQueue<Callable<GloryDE50StateAbstract>>();

    public WaitForOperation(GloryDE50Device.GloryDE50StateMachineApi api) {
        super(api);
    }

    // The trick to avoid race conditions here is that count only change a variable in the state or fails
    // the outside thread is not able to change the state.
    @Override
    public boolean count(final Map<Integer, Integer> desiredQuantity, final Integer currency) {
        return comunicate(new Callable<GloryDE50StateAbstract>() {
            public GloryDE50StateAbstract call() throws Exception {
                return new Count(api, desiredQuantity, currency);
            }
        });
    }

    @Override
    public boolean envelopeDeposit() {
        return comunicate(new Callable<GloryDE50StateAbstract>() {
            public GloryDE50StateAbstract call() throws Exception {
                return new EnvelopeDeposit(api);
            }
        });
    }

    @Override
    public boolean collect() {
        return comunicate(new Callable<GloryDE50StateAbstract>() {
            public GloryDE50StateAbstract call() throws Exception {
                return new Collect(api);
            }
        });
    }

    @Override
    public boolean openPort(final String pvalue, boolean wait) {
        final GloryDE50StateAbstract openop = new OpenPort(api);
        boolean ret = comunicate(new Callable<GloryDE50StateAbstract>() {
            public GloryDE50StateAbstract call() throws Exception {
                Logger.debug("switch to open port");
                return openop;
            }
        });
        if (!ret) {
            return false;
        }
        return openop.openPort(pvalue, wait);
    }
}
