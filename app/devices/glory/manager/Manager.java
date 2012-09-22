package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.command.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class Manager {

    static public enum Status {

        IDLE,
        READY_TO_STORE,
        ERROR,
        PUT_THE_BILLS_ON_THE_HOPER,
        ESCROW_FULL,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,
        CANCELING, CANCELED,;
    };

    static public enum Error {

        APP_ERROR,
        JAM,
        STORING_ERROR_CALL_ADMIN,
        BILLS_IN_ESCROW_CALL_ADMIN;
    };

    static public class ErrorDetail {

        public Error code;
        public String data;

        public ErrorDetail(Error code, String data) {
            this.code = code;
            this.data = data;
        }

        @Override
        public String toString() {
            return "ErrorDetail{" + "code=" + code + ", data=" + data + '}';
        }
    }
    final private Thread thread;
    final private Glory device;
    final private ManagerThreadState managerThreadState;
    // TODO: Better error reporting, as a class with arguments.
    final private AtomicReference<Status> status = new AtomicReference<Status>(Status.IDLE);
    final private AtomicReference<ErrorDetail> error = new AtomicReference<ErrorDetail>();
    // TODO: Change onCountDont to an observable.

    public Manager(Glory device) {
        this.device = device;
        this.managerThreadState = new ManagerThreadState();
        this.thread = new ManagerThread(new ThreadCommandApi());
    }
    /*
     *
     *
     * Thread API
     *
     *
     */

    public class ThreadCommandApi {

        private final ManagerThreadState.ThreadApi managerThreadApi;

        public ThreadCommandApi() {
            managerThreadApi = managerThreadState.getThreadApi();
        }

        public ManagerCommandAbstract getCurrentCommand() {
            return managerThreadApi.getCurrentCommand();
        }

        public boolean mustStop() {
            return managerThreadApi.mustStop();
        }

        void stopped() {
            managerThreadApi.stopped();
        }

        public GloryCommandAbstract sendGloryCommand(GloryCommandAbstract cmd, boolean debug) {
            return device.sendCommand(cmd, debug);
        }

        public Status getStatus() {
            return status.get();
        }

        public void setStatus(Status s) {
            // TODO: This is not thread safe. Review
            if (status.get() != Status.ERROR) {
                status.set(s);
            }
        }

        public void setStatus(Status c, Status s) {
            if (status.get() != Status.ERROR) {
                status.compareAndSet(c, s);
            }
        }

        public void setError(Manager.ErrorDetail e) {
            error.set(e);
        }
    }

    /*
     *
     *
     * Controller API
     *
     *
     */
    public class ControllerApi {

        private final ManagerThreadState.ControllerApi managerControllerApi;
        private final ThreadCommandApi threadCommandApi = new ThreadCommandApi();

        public ControllerApi() {
            managerControllerApi = managerThreadState.getControllerApi();
        }

        public boolean count(Runnable onCommandDone, Map<Integer, Integer> desiredQuantity, Integer currency) {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof Count) {
                    return true;
                }
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new Count(threadCommandApi, onCommandDone, desiredQuantity, currency));
        }

        public Integer getCurrency() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof Count)) {
                return null;
            }
            return ((Count) cmd).getCurrency();
        }

        public Map<Integer, Integer> getCurrentQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof Count)) {
                return null;
            }
            return ((Count) cmd).getCurrentQuantity();
        }

        public Map<Integer, Integer> getDesiredQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof Count)) {
                return null;
            }
            return ((Count) cmd).getDesiredQuantity();
        }

        // TODO: Fix this.
        public boolean cancelDeposit(Runnable onCommandDone) {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return managerControllerApi.sendCommand(new CancelCount(threadCommandApi, onCommandDone));
            }
            // TODO: One base class
            if (!(cmd instanceof Count) && !(cmd instanceof EnvelopeDeposit)) {
                return false;
            }
            cmd.cancel();
            return true;
        }

        public boolean storeDeposit(Integer sequenceNumber) {
            Logger.debug("storeDeposit");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return true;
            }
            // TODO: One base class
            if (cmd instanceof Count) {
                ((Count) cmd).storeDeposit(sequenceNumber);
                return true;
            }
            return false;
        }

        public boolean withdrawDeposit() {
            Logger.debug("withDrawDeposit");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return true;
            }
            // TODO: One base class
            if (cmd instanceof Count) {
                ((Count) cmd).withdrawDeposit();
                return true;
            }
            return false;
        }

        public boolean envelopeDeposit(Runnable onCommandDone) {
            Logger.debug("envelopeDeposit");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof EnvelopeDeposit) {
                    return true;
                }
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new EnvelopeDeposit(threadCommandApi, onCommandDone));
        }

        public boolean reset(Runnable onCommandDone) {
            Logger.debug("------reset");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof Reset) {
                    return true;
                }
                cmd.cancel();
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new Reset(threadCommandApi, onCommandDone));
        }

        public boolean storingErrorReset(Runnable onCommandDone) {
            Logger.debug("------storing error reset");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof StoringErrorReset) {
                    return true;
                }
                cmd.cancel();
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new StoringErrorReset(threadCommandApi, onCommandDone));
        }

        public Manager.Status getStatus() {
            return status.get();
        }

        public ErrorDetail getErrorDetail() {
            return error.get();
        }
    }
    /*
     *
     *
     * Counter Factory API
     *
     *
     */

    public class CounterFactoryApi {

        public void startThread() {
            thread.start();
        }

        public void close() {
            Logger.debug("Closing Manager Stop");
//            if (thread.isAlive()) {
            if (thread != null && thread.isAlive()) {
                managerThreadState.stop();
                try {
                    Logger.debug("Closing Manager waitUntilStop");
                    managerThreadState.waitUntilStop(60000);
                    thread.join(10000);
                } catch (InterruptedException ex) {
                    Logger.info("Manager thread don't die");
                }
            }
            Logger.debug("Closing Manager done");
        }

        public ControllerApi getControllerApi() {
            return new ControllerApi();
        }
    }

    public CounterFactoryApi getCounterFactoryApi() {
        return new CounterFactoryApi();
    }
}
