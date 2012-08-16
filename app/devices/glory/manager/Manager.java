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
    
    public enum Status {
        
        IDLE,
        READY_TO_STORE,
        ERROR,
        PUT_THE_BILLS_ON_THE_HOPER,
        ESCROW_FULL,
        PUT_THE_ENVELOPER_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,;
    };
    final private Thread thread;
    final private Glory device;
    final private ManagerThreadState managerThreadState;
    // TODO: Better error reporting, as a class with arguments.
    final private AtomicReference<Status> status = new AtomicReference<Status>();
    final private AtomicReference<String> error = new AtomicReference<String>();
    
    public Manager(Glory device) {
        this.device = device;
        this.managerThreadState = new ManagerThreadState();
        this.thread = new Thread(new ManagerThread(new ThreadCommandApi()));
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
        
        public GloryCommandAbstract sendGloryCommand(GloryCommandAbstract cmd) {
            return device.sendCommand(cmd);
        }
        
        public void setStatus(Status s) {
            status.set(s);
        }
        
        public void setStatus(Status c, Status s) {
            status.compareAndSet(c, s);
        }
        
        public void setError(String e) {
            error.set(e);
            setStatus(Status.ERROR);
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
        
        public boolean count(Map<Integer, Integer> desiredQuantity) {
            if (managerControllerApi.getCurrentCommand() != null) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new Count(threadCommandApi, desiredQuantity));
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
        
        public boolean cancelDeposit() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                return managerControllerApi.sendCommand(new CancelCount(threadCommandApi));
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
        
        public boolean envelopeDeposit() {
            Logger.debug("envelopeDeposit");
            if (managerControllerApi.getCurrentCommand() != null) {
                // still executing
                return false;
            }
            threadCommandApi.setStatus(Manager.Status.PUT_THE_ENVELOPER_IN_THE_ESCROW);
            return managerControllerApi.sendCommand(new EnvelopeDeposit(threadCommandApi));
        }
        
        public boolean reset() {
            Logger.debug("------reset");
            if (managerControllerApi.getCurrentCommand() != null) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new Reset(threadCommandApi));
        }
        
        public boolean storingErrorReset() {
            Logger.debug("------storing error reset");
            if (managerControllerApi.getCurrentCommand() != null) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand(new StoringErrorReset(threadCommandApi));
        }
        
        public Manager.Status getStatus() {
            return status.get();
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
            if (thread.isAlive()) {
                managerThreadState.stop();
                try {
                    Logger.debug("Closing Manager waitUntilStop");
                    managerThreadState.waitUntilStop(60000);
                    thread.join(1000);
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
