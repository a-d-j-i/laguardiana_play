/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.ManagerInterface;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import play.Logger;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract implements Runnable {

    final static boolean DEBUG = false;
    final static int retries = 0xfff;

    static protected class CommandData {

        private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock r = rwl.readLock();
        private final Lock w = rwl.writeLock();
        private boolean storeDeposit = false;

        protected boolean needToStoreDeposit() {
            rlock();
            try {
                return storeDeposit;
            } finally {
                runlock();
            }
        }

        protected void storeDeposit() {
            wlock();
            try {
                this.storeDeposit = true;
            } finally {
                wunlock();
            }
        }

        protected void storeDepositDone() {
            wlock();
            try {
                this.storeDeposit = false;
            } finally {
                wunlock();
            }
        }

        final protected void rlock() {
            r.lock();
        }

        final protected void runlock() {
            r.unlock();
        }

        final protected void wlock() {
            w.lock();
        }

        final protected void wunlock() {
            w.unlock();
        }
    }
    protected final GloryStatus gloryStatus = new GloryStatus();
    private final ThreadCommandApi threadCommandApi;

    public ManagerCommandAbstract(ThreadCommandApi threadCommandApi) {
        this.threadCommandApi = threadCommandApi;
    }
    private AtomicBoolean isDone = new AtomicBoolean(false);
    private AtomicBoolean mustCancel = new AtomicBoolean(false);

    abstract public void execute();

    public void run() {
        isDone.set(false);
        execute();

        switch (threadCommandApi.getState()) {
            case ERROR:
                break;
            default:
                if (mustCancel()) {
                    threadCommandApi.setState(ManagerInterface.State.CANCELED);
                }
                threadCommandApi.setState(ManagerInterface.State.IDLE);
        }
        isDone.set(true);
    }

    public boolean isDone() {
        return isDone.get();
    }

    public void cancel() {
        mustCancel.set(true);
    }

    public boolean mustCancel() {
        return (mustCancel.get() || threadCommandApi.mustStop());
    }

    boolean gotoNeutral(boolean openEscrow, boolean forceEmptyHoper) {
        for (int i = 0; i < retries; i++) {
            boolean avoidCancel = false;
            Logger.debug("GOTO NEUTRAL %s %s",
                    (openEscrow ? "OPEN ESCROW" : ""),
                    (forceEmptyHoper ? "FORCE EMPTY HOPER" : ""));
            if (!sense()) {
                return false;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_error:
                    setError(ManagerInterface.Error.STORING_ERROR_CALL_ADMIN, "Storing error must call admin");
                    return false;
            }

            switch (gloryStatus.getD1Mode()) {
                case normal_error_recovery_mode:
                case storing_error_recovery_mode:
                case deposit:
                case collect_mode:
                case manual:
                    switch (gloryStatus.getSr1Mode()) {
                        case escrow_open_request:
                            if (!openEscrow) {
                                setError(ManagerInterface.Error.BILLS_IN_ESCROW_CALL_ADMIN,
                                        "There are bills in the escrow call an admin");
                                return false;
                            }
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                break;
                            }
                            avoidCancel = true;
                            break;
                        case abnormal_device:
                            setState(ManagerInterface.State.JAM);
                            if (gloryStatus.getD1Mode() == GloryStatus.D1Mode.normal_error_recovery_mode) {
                                resetDevice();
                            } else {
                                if (!sendGCommand(new devices.glory.command.RemoteCancel())) {
                                    Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastError());
                                    return false;
                                }
                            }
                            break;
                        case escrow_close: // The escrow is closing... wait.
                            avoidCancel = true;
                            break;
                        case storing_start_request:
                            if (!openEscrow) {
                                setError(ManagerInterface.Error.BILLS_IN_ESCROW_CALL_ADMIN,
                                        "There are bills in the escrow call an admin");
                                return false;
                            }
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                break;
                            }
                            avoidCancel = true;
                        // dont break
                        case escrow_open:
                        case escrow_close_request:
                        case being_restoration:
                        case being_recover_from_storing_error:
                        case waiting_for_an_envelope_to_set:
                            avoidCancel = true;
                            WaitForEmptyEscrow();
                            sendRemoteCancel();
                            break;
                        case counting_start_request:
                        case being_exchange_the_cassette:
                        case waiting:
                            sendRemoteCancel();
                            break;
                        case counting:
                            // Japaneese hack...
                            break;
                        default:
                            setError(ManagerInterface.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name()));
                            break;
                    }
                    break;
                case initial:
                    if (sendRemoteCancel()) {
                        if (gloryStatus.getD1Mode() != GloryStatus.D1Mode.neutral) {
                            setError(ManagerInterface.Error.APP_ERROR,
                                    String.format("cant set neutral mode d1 (%s) mode not neutral", gloryStatus.getD1Mode().name()));
                        }
                    }
                    break;
                case neutral:
                    switch (gloryStatus.getSr1Mode()) {
                        case abnormal_device:
                            setState(ManagerInterface.State.JAM);
                            if (!sendGCommand(new devices.glory.command.SetErrorRecoveryMode())) {
                                setError(ManagerInterface.Error.APP_ERROR,
                                        String.format("gotoNeutral Error setting normal error recovery mode Error %s", gloryStatus.getLastError()));
                                return false;
                            }
                            break;
                        case waiting:
                            if (!forceEmptyHoper) {
                                Logger.debug("GOTO NEUTRAL DONE");
                                return true;
                            } else {
                                if (gloryStatus.isRejectBillPresent()) {
                                    setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
                                } else if (gloryStatus.isHopperBillPresent()) {
                                    setState(ManagerInterface.State.REMOVE_THE_BILLS_FROM_HOPER);
                                } else {
                                    Logger.debug("GOTO NEUTRAL DONE");
                                    return true;
                                }
                            }
                            break;
                        default:
                            setError(ManagerInterface.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name()));
                            break;
                    }
                    break;
                default:
                    setError(ManagerInterface.Error.APP_ERROR,
                            String.format("gotoNeutralInvalid D1-4 mode %s", gloryStatus.getD1Mode().name()));
                    break;
            }
            if (mustCancel() && !avoidCancel) {
                break;
            }
            sleep();
        }
        if (!mustCancel()) {
            setError(ManagerInterface.Error.APP_ERROR, "GOTO NEUTRAL TIMEOUT");
            Logger.debug("GOTO NEUTRAL TIMEOUT!!!");
        }

        Logger.debug("GOTO NEUTRAL DONE CANCEL");
        return false;
    }

    boolean sendGloryCommand(GloryCommandAbstract cmd) {
        if (cmd != null) {
            if (!sendGCommand(cmd)) {
                String error = gloryStatus.getLastError();
                Logger.error("Error %s sending cmd : %s", error, cmd.getDescription());
                setError(ManagerInterface.Error.APP_ERROR, error);
                return false;
            }
        }
        return sense();
    }

    boolean sense() {
        if (!sendGCommand(new devices.glory.command.Sense())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : SENSE", error);
            setError(ManagerInterface.Error.APP_ERROR, error);
            return false;
        }
        Logger.debug(String.format("D1Mode %s SR1 Mode : %s", gloryStatus.getD1Mode().name(), gloryStatus.getSr1Mode().name()));
        return true;
    }

    boolean sendGCommand(GloryCommandAbstract cmd) {
        if (cmd == null) {
            setError(ManagerInterface.Error.APP_ERROR, "Invalid command null");
            return false;
        }
        return gloryStatus.setStatusOk(threadCommandApi.sendGloryCommand(cmd, DEBUG));
    }

    void resetDevice() {
        if (!sendGloryCommand(new devices.glory.command.ResetDevice())) {
            return;
        }

        while (!mustCancel()) {
            Logger.debug("errorRecovery");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case being_reset:
                case being_restoration:
                    sleep();
                    break;
                default:
                    return;
            }
        }
    }

    void WaitForEmptyEscrow() {
        for (int i = 0; i < retries; i++) {
            Logger.debug("WaitForEmptyEscrow");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case escrow_close_request:
                    if (gloryStatus.isEscrowBillPresent()) {
                        break;
                    }
                case being_recover_from_storing_error:
                case waiting_for_an_envelope_to_set:
                    if (!sendGloryCommand(new devices.glory.command.CloseEscrow())) {
                        return;
                    }
                    break;
                case being_restoration:
                case counting:
                case escrow_close:
                    break;
                case escrow_open:
                    setState(ManagerInterface.State.REMOVE_THE_BILLS_FROM_ESCROW);
                    break;
                case counting_start_request:
                case waiting:
                    return;
                case abnormal_device:
                    setState(ManagerInterface.State.JAM);
                    return;
                case storing_start_request:
                case storing_error:
                    setError(ManagerInterface.Error.STORING_ERROR_CALL_ADMIN,
                            "Storing error, todo: get the flags");
                    return;
                default:
                    setError(ManagerInterface.Error.APP_ERROR,
                            String.format("WaitForEmptyEscrow invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    break;
            }
            sleep();
        }
        setError(ManagerInterface.Error.APP_ERROR,
                "WaitForEmptyEscrow waiting too much");
    }

    protected boolean sendRemoteCancel() {
        // Under this conditions the remoteCancel command fails.
        if (gloryStatus.isRejectBillPresent()) {
            setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
            return false;
        }
        if (gloryStatus.isHopperBillPresent()) {
            setState(ManagerInterface.State.REMOVE_THE_BILLS_FROM_HOPER);
            return false;
        }
        switch (gloryStatus.getSr1Mode()) {
            case counting:
            case storing_start_request:
                return false;
            default:
                if (!sendGCommand(new devices.glory.command.RemoteCancel())) {
                    Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastError());
                    return false;
                }
                return sense();
        }
    }

    void sleep() {
        sleep(500);
    }

    void sleep(int timems) {
        try {
            Thread.sleep(timems);
        } catch (InterruptedException ex) {
        }
    }

    protected void setError(ManagerInterface.Error e, String s) {
        Logger.error("MANAGER ERROR : %s %s", e.name(), s);
        threadCommandApi.setErrorInfo(e, s);
        setState(ManagerInterface.State.ERROR);
    }

    protected void setState(ManagerInterface.State state) {
        threadCommandApi.setState(state);
    }

    protected void clearError() {
        threadCommandApi.clearError();
    }
}
