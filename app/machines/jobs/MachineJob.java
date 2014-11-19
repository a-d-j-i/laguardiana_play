package machines.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import machines.MachineAbstract;
import machines.MachineInterface;
import play.Logger;
import play.jobs.Job;
import play.libs.F;

/**
 * Make all the access to the facade sequential.
 *
 * @author adji
 * @param <V>
 */
public class MachineJob<V> extends Job<V> {

    public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    protected final MachineAbstract machine;

    public MachineJob(MachineInterface machine) {
        this.machine = (MachineAbstract) machine;
    }

    @Override
    public F.Promise<V> now() {
        final F.Promise<V> smartFuture = new F.Promise<V>();
        singleThreadExecutor.submit(new Callable<V>() {
            public V call() throws Exception {
                V result = MachineJob.this.call();
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
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            Logger.error("InterruptedException inm runNow : %s : %s", ex.toString(), errors.toString());
        } catch (ExecutionException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            Logger.error("ExecutionException inm runNow : %s : %s", ex.toString(), errors.toString());
        }
        return null;
    }

//    public boolean isBagReady(boolean envelope) {
//
//        if (Configuration.isIgnoreBag()) {
//            return true;
//        }
//        if (!machine.isBagReady()) {
//            Logger.info("Can't start bag removed");
//            return false;
//        }
//        LgBag currentBag = LgBag.getCurrentBag();
//        ItemQuantity iq = currentBag.getItemQuantity();
//        // for an envelope deposit I neet at least space for one envelope more.
//        if (envelope) {
//            iq.envelopes++;
//            iq.bills--;
//        }
//        Logger.debug("isBagReady quantity : %s", iq);
//        if (Configuration.isBagFull(iq.bills, iq.envelopes)) {
//            Logger.info("Can't start bag full");
//            //modelError.setError(ModelError.ERROR_CODE.BAG_FULL, "Bag full too many bills and evenlopes");
//            return false;
//        }
//        return true;
//    }
}
