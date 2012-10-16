package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.command.*;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryManager {

    static public enum State {

        IDLE,
        READY_TO_STORE,
        STORING,
        ERROR,
        PUT_THE_BILLS_ON_THE_HOPER,
        COUNTING,
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

    static public class Status extends Observable {

        private State state = State.IDLE;
        private Error error;
        private String errorMsg;

        @Override
        synchronized public String toString() {
            return "Error ( " + error + " ) : " + errorMsg;
        }

        synchronized public State getState() {
            return state;
        }

        synchronized private void setState(State state) {
            if (this.state != State.ERROR) {
                if (this.state != state) {
                    setChanged();
                }
                this.state = state;
                notifyObservers(this);
            }

        }

        synchronized private void clearError() {
            if (state == State.ERROR) {
                this.state = State.IDLE;
                setChanged();
                notifyObservers(this);
            }
        }

        synchronized public String getErrorDetail() {
            return "ErrorDetail{" + "code=" + error + ", data=" + errorMsg + '}';
        }

        synchronized private void setError(Error e, String msg) {
            this.error = e;
            this.errorMsg = msg;
        }

        synchronized public String name() {
            return state.name();
        }
    }
    final private Thread thread;
    final private Glory device;
    final private ManagerThreadState managerThreadState;
    final private Status status = new Status();

    public GloryManager(Glory device) {
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

        public State getState() {
            return status.getState();
        }

        public void setState(State s) {
            status.setState(s);
        }

        public void clearError() {
            status.clearError();
        }

        public void setErrorInfo(Error e, String msg) {
            status.setError(e, msg);
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

        public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof CountCommand) {
                    return true;
                }
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new CountCommand(threadCommandApi, desiredQuantity, currency));
        }

        public Integer getCurrency() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof CountCommand)) {
                return null;
            }
            return ((CountCommand) cmd).getCurrency();
        }

        public Map<Integer, Integer> getCurrentQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof CountCommand)) {
                return null;
            }
            return ((CountCommand) cmd).getCurrentQuantity();
        }

        public Map<Integer, Integer> getDesiredQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return null;
            }
            if (!(cmd instanceof CountCommand)) {
                return null;
            }
            return ((CountCommand) cmd).getDesiredQuantity();
        }

        // TODO: Fix this.
        public boolean cancelDeposit() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                Logger.debug("cancelDeposit executing cancelcount");
                return managerControllerApi.sendCommand(new CancelCountCommand(threadCommandApi));
            }
            // TODO: One base class
            if (!(cmd instanceof CountCommand) && !(cmd instanceof EnvelopeDepositCommand)) {
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
            if (cmd instanceof CountCommand) {
                ((CountCommand) cmd).storeDeposit(sequenceNumber);
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
            if (cmd instanceof CountCommand) {
                ((CountCommand) cmd).withdrawDeposit();
                return true;
            }
            return false;
        }

        public boolean envelopeDeposit() {
            Logger.debug("envelopeDeposit");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof EnvelopeDepositCommand) {
                    return true;
                }
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new EnvelopeDepositCommand(threadCommandApi));
        }

        public boolean reset() {
            Logger.debug("reset");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof ResetCommand) {
                    Logger.debug("reset RUNNING");
                    return true;
                }
                cmd.cancel();
                // still executing
                Logger.debug("cmd still executing");
                return false;
            }
            Logger.debug("executing reset");
            return managerControllerApi.sendCommand(new ResetCommand(threadCommandApi));
        }

        public boolean storingErrorReset() {
            Logger.debug("storing error reset");
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof StoringErrorResetCommand) {
                    return true;
                }
                cmd.cancel();
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new StoringErrorResetCommand(threadCommandApi));
        }

        public Status getStatus() {
            return status;
        }

        public void addObserver(Observer observer) {
            status.addObserver(observer);
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
                    managerThreadState.waitUntilStop(Glory.GLORY_READ_TIMEOUT * 2);
                    thread.join(Glory.GLORY_READ_TIMEOUT * 2);
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
