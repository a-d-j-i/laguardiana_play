package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.ManagerInterface.MANAGER_STATE;
import devices.glory.manager.command.*;
import java.util.Map;
import java.util.Observer;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryManager {

    final private Thread thread;
    final private Glory device;
    final private ManagerThreadState managerThreadState;
    final private static ManagerInterface.State state = new ManagerInterface.State();

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

        void currentCommandDone() {
            managerThreadApi.currentCommandDone();
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

        public MANAGER_STATE getState() {
            return state.getStatus().getState();
        }

        public void setState(Map<Integer, Integer> bills) {
            state.setState(bills);
        }

        public void setState(MANAGER_STATE s) {
            state.setState(s);
        }

        public void clearError() {
            state.clearError();
        }

        public void setError(GloryManagerError e) {
            state.setError(e);
        }

        public boolean isClosing() {
            return state.isClosing();
        }

        public void setClosing(boolean closing) {
            state.setClosing(closing);
        }
    }

    /*
     *
     *
     * Controller API
     *
     *
     */
    public class ManagerControllerApi implements ManagerInterface {

        private final ManagerThreadState.ControllerApi managerControllerApi;
        private final ThreadCommandApi threadCommandApi = new ThreadCommandApi();

        public ManagerControllerApi() {
            managerControllerApi = managerThreadState.getControllerApi();
        }

        public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
            return managerControllerApi.sendCommand(new CountCommand(threadCommandApi, desiredQuantity, currency));
        }

        public boolean envelopeDeposit() {
            return managerControllerApi.sendCommand(new EnvelopeDepositCommand(threadCommandApi));
        }

        // syncronous cancel and collect.
        public boolean collect() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                Logger.debug("Collect canceling last command");
                cmd.cancel();
                try {
                    managerControllerApi.waitUntilWaiting(120 * 1000);
                } catch (InterruptedException ex) {
                    return false;
                }
                cmd = managerControllerApi.getCurrentCommand();
                if (cmd != null) {
                    return false;
                }
                Logger.debug("Collect last command canceled");
            }
            Logger.debug("Collect send collect to glory");
            if (!managerControllerApi.sendCommand(new CollectCommand(threadCommandApi))) {
                return false;
            }
            try {
                managerControllerApi.waitUntilWaiting(120 * 1000);
            } catch (InterruptedException ex) {
                return false;
            }
            Logger.debug("Collect send collect to glory done");
            cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                return false;
            }
            Logger.debug("Collect done");
            return true;
        }

        public boolean reset() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof ResetCommand) {
                    return true;
                }
                cmd.cancel();
                try {
                    managerControllerApi.waitUntilWaiting(10000);
                } catch (InterruptedException ex) {
                }
                return false;
            }
            return managerControllerApi.sendCommand(new ResetCommand(threadCommandApi));
        }

        public boolean storingErrorReset() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd != null) {
                if (cmd instanceof StoringErrorResetCommand) {
                    return true;
                }
                cmd.cancel();
                return false;
            }
            return managerControllerApi.sendCommand(new StoringErrorResetCommand(threadCommandApi));
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

        public void cancelCommand() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if (cmd == null) {
                managerControllerApi.sendCommand(new GotoNeutral(threadCommandApi));
                return;
            }
            cmd.cancel();
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
            } else if (cmd instanceof EnvelopeDepositCommand) {
                ((EnvelopeDepositCommand) cmd).storeDeposit(sequenceNumber);
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

        public ManagerStatus getStatus() {
            return new ManagerStatus(state);
        }

        public void addObserver(Observer observer) {
            state.addObserver(observer);
        }

        public Glory getCounter() {
            return device;
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

        public ManagerControllerApi getControllerApi() {
            return new ManagerControllerApi();
        }
    }

    public CounterFactoryApi getCounterFactoryApi() {
        return new CounterFactoryApi();
    }
}
