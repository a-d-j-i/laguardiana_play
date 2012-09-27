/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.GloryStatus.SR1Mode;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManager.ThreadCommandApi;
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

    final static boolean DEBUG = true;
    final static int retries = 0xfff;

    static protected class CommandData {

        private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock r = rwl.readLock();
        private final Lock w = rwl.writeLock();

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
    protected final Runnable onCommandDone;

    public ManagerCommandAbstract(ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        this.threadCommandApi = threadCommandApi;
        this.onCommandDone = onCommandDone;
    }
    private AtomicBoolean isDone = new AtomicBoolean(false);
    private AtomicBoolean mustCancel = new AtomicBoolean(false);

    abstract public void execute();

    public void run() {
        isDone.set(false);
        execute();

        switch (threadCommandApi.getStatus()) {
            case ERROR:
                if (onCommandDone != null) {
                    try {
                        onCommandDone.run();
                    } catch (Exception e) {
                        setError(GloryManager.Error.APP_ERROR, e.getMessage());
                    }
                }
                break;
            default:
                if (mustCancel()) {
                    threadCommandApi.setStatus(GloryManager.Status.CANCELED);
                } else {
                    threadCommandApi.setStatus(GloryManager.Status.IDLE);
                }
                if (onCommandDone != null) {
                    try {
                        onCommandDone.run();
                    } catch (Exception e) {
                        setError(GloryManager.Error.APP_ERROR, e.getMessage());
                    }
                }
                threadCommandApi.setStatus(GloryManager.Status.IDLE);
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

    boolean gotoNeutral(boolean openEscrow, boolean storingError) {
        for (int i = 0; i < retries; i++) {
            Logger.debug("GOTO NEUTRAL");
            if (!sense()) {
                return false;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_error:
                    if (!storingError) {
                        setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN,
                                "Storing error must call admin");
                        return false;
                    }
                    switch (gloryStatus.getD1Mode()) {
                        case storing_error_recovery_mode:
                        case normal_error_recovery_mode:
                        case deposit:
                        case manual:
                            if (canSendRemoteCancel()) {
                                if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                    return false;
                                }
                            }
                        // dont break;
                        case neutral:
                            storingErrorRecovery();
                            break;
                        default:
                            setError(GloryManager.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid D1 mode %s", gloryStatus.getD1Mode().name()));
                            return false;
                    }
                    break;

                case abnormal_device:
                    switch (gloryStatus.getD1Mode()) {
                        case deposit:
                        case manual:
                            if (canSendRemoteCancel()) {
                                if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                    return false;
                                }
                            }
                        // dont break;
                        case neutral:
                            if (!sendGloryCommand(new devices.glory.command.SetErrorRecoveryMode())) {
                                return false;
                            }
                            break;
                        case normal_error_recovery_mode:
                            break;
                        default:
                            setError(GloryManager.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid D1-1 mode %s", gloryStatus.getD1Mode().name()));
                            return false;
                    }
                    errorRecovery();
                    break;
                default:
                    // Above are errors, rest is ok.
                    break;
            }

            switch (gloryStatus.getD1Mode()) {
                case normal_error_recovery_mode:
                case storing_error_recovery_mode:
                case deposit:
                case manual:
                    switch (gloryStatus.getSr1Mode()) {
                        case escrow_close: // The escrow is closing... wait.
                            break;
                        case storing_start_request:
                            if (!openEscrow) {
                                setError(GloryManager.Error.BILLS_IN_ESCROW_CALL_ADMIN,
                                        "There are bills in the escrow call an admin");
                                return false;
                            }
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                break;
                            }
                        // dont break
                        case escrow_open:
                        case escrow_close_request:
                        case being_restoration:
                        case waiting_for_an_envelope_to_set:
                            WaitForEmptyEscrow();
                            if (canSendRemoteCancel()) {
                                if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                    return false;
                                }
                            }
                            break;
                        case being_recover_from_storing_error:
                            if (storingError) {
                                errorRecovery();
                            } else {
                                WaitForEmptyEscrow();
                                setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN, "Storing error must call admin");
                                if (canSendRemoteCancel()) {
                                    if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                        return false;
                                    }
                                }
                            }
                            break;
                        case counting_start_request:
                            if (canSendRemoteCancel()) {
                                if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                    return false;
                                }
                            }
                            break;
                        case counting:
                            // Japaneese hack...
                            break;
                        case waiting:
                            if (canSendRemoteCancel()) {
                                if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                    return false;
                                }
                            }
                            break;
                        default:
                            setError(GloryManager.Error.APP_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name()));
                            break;
                    }
                    break;
                case initial:
                    if (canSendRemoteCancel()) {
                        if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                            return false;
                        }
                    }
                    if (gloryStatus.getD1Mode() != GloryStatus.D1Mode.neutral) {
                        setError(GloryManager.Error.APP_ERROR,
                                String.format("cant set neutral mode d1 (%s) mode not neutral", gloryStatus.getD1Mode().name()));
                    }
                    break;
                case neutral:
                    // Wait until bills are from anyware removed.
                    if (!canSendRemoteCancel()) {
                        break;
                    }
                    Logger.debug("GOTO NEUTRAL DONE");
                    return true;
                default:
                    setError(GloryManager.Error.APP_ERROR,
                            String.format("gotoNeutralInvalid D1-4 mode %s", gloryStatus.getD1Mode().name()));
                    break;
            }
            sleep();
        }
        setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN, "GOTO NEUTRAL TIMEOUT");
        Logger.debug("GOTO NEUTRAL TIMEOUT!!!");
        return false;
    }

    boolean sendGloryCommand(GloryCommandAbstract cmd) {
        if (cmd != null) {
            if (!sendGCommand(cmd)) {
                String error = gloryStatus.getLastError();
                Logger.error("Error %s sending cmd : %s", error, cmd.getDescription());
                setError(GloryManager.Error.APP_ERROR, error);
                return false;
            }
        }
        return sense();
    }

    boolean sense() {
        if (!sendGCommand(new devices.glory.command.Sense())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : SENSE", error);
            setError(GloryManager.Error.APP_ERROR, error);
            return false;
        }
        Logger.debug(String.format("D1Mode %s SR1 Mode : %s", gloryStatus.getD1Mode().name(), gloryStatus.getSr1Mode().name()));
        return true;
    }

    boolean sendGCommand(GloryCommandAbstract cmd) {
        if (cmd == null) {
            setError(GloryManager.Error.APP_ERROR, "Invalid command null");
            return false;
        }
        return gloryStatus.setStatusOk(threadCommandApi.sendGloryCommand(cmd, DEBUG));
    }

    void errorRecovery() {
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

    void storingErrorRecovery() {
        if (!sendGloryCommand(new devices.glory.command.SetStroringErrorRecoveryMode())) {
            return;
        }
        waitUntilD1State(D1Mode.storing_error_recovery_mode);
        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
            return;
        }
        // TODO: Review.
        waitUntilSR1State(SR1Mode.being_recover_from_storing_error);
        if (!sendGloryCommand(new devices.glory.command.ResetDevice())) {
            return;
        }
        waitUntilSR1State(SR1Mode.escrow_close_request);
        // Close escrow
        WaitForEmptyEscrow();
        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
            return;
        }
        waitUntilSR1State(SR1Mode.waiting);
        gotoNeutral(true, false);
    }

    boolean waitUntilSR1State(SR1Mode state) {
        for (int i = 0; i < retries; i++) {
            Logger.debug("waitUntilSR1State %s", state.name());
            if (mustCancel()) {
                return true;
            }
            if (!sense()) {
                return false;
            }
            SR1Mode m = gloryStatus.getSr1Mode();
            if (m == state) {
                return true;
            }
            switch (m) {
                case abnormal_device:
                    setError(GloryManager.Error.JAM,
                            "waitUntilSR1State Abnormal device, todo: get the flags");
                    return false;
                case storing_error:
                    setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN,
                            "waitUntilSR1State Storing error, todo: get the flags");
                    return false;
                default:
                    break;
            }
            sleep();
        }
        setError(GloryManager.Error.APP_ERROR,
                String.format("cant set sr1 mode to %s", state.name()));
        return false;
    }

    boolean waitUntilD1State(D1Mode state) {
        for (int i = 0; i < retries; i++) {
            Logger.debug("waitUntilD1State %s", state.name());
            if (mustCancel()) {
                return true;
            }
            if (!sense()) {
                return false;
            }
            if (gloryStatus.getD1Mode() == state) {
                return true;
            }
            switch (gloryStatus.getSr1Mode()) {
                case abnormal_device:
                    setError(GloryManager.Error.JAM,
                            "waitUntilD1State Abnormal device, todo: get the flags");
                    return false;
                case storing_error:
                    setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN,
                            "waitUntilD1State Storing error, todo: get the flags");
                    return false;
                default:
                    break;
            }
            sleep();
        }
        setError(GloryManager.Error.APP_ERROR,
                String.format("cant set d1 mode to %s", state.name()));
        return false;
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
                    threadCommandApi.setStatus(GloryManager.Status.REMOVE_THE_BILLS_FROM_ESCROW);
                    break;
                case counting_start_request:
                case waiting:
                    return;
                case abnormal_device:
                    setError(GloryManager.Error.JAM,
                            "Abnormal device, todo: get the flags");
                    return;
                case storing_start_request:
                case storing_error:
                    setError(GloryManager.Error.STORING_ERROR_CALL_ADMIN,
                            "Storing error, todo: get the flags");
                    return;
                default:
                    setError(GloryManager.Error.APP_ERROR,
                            String.format("WaitForEmptyEscrow invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    break;
            }
            sleep();
        }
        setError(GloryManager.Error.APP_ERROR,
                "WaitForEmptyEscrow waiting too much");
    }

    private boolean canSendRemoteCancel() {
        // Under this conditions the remoteCancel command fails.
        if (gloryStatus.isRejectBillPresent()) {
            threadCommandApi.setStatus(GloryManager.Status.REMOVE_REJECTED_BILLS);
            return false;
        }
        if (gloryStatus.isHopperBillPresent()) {
            threadCommandApi.setStatus(GloryManager.Status.REMOVE_THE_BILLS_FROM_HOPER);
            return false;
        }
        switch (gloryStatus.getSr1Mode()) {
            case counting:
            case storing_start_request:
                return false;
            default:
                return true;
        }
    }

    void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }

    protected void setError(GloryManager.Error e, String s) {
        Logger.error("MANAGER ERROR : %s %s", e.name(), s);
        threadCommandApi.setError(new GloryManager.ErrorDetail(e, s));
        setStatus(GloryManager.Status.ERROR, true);
    }

    protected void setStatus(GloryManager.Status status, boolean publish) {
        GloryManager.Status oldStatus = threadCommandApi.getStatus();
        threadCommandApi.setStatus(status);

        if (oldStatus != status && publish) {
            if (onCommandDone != null) {
                try {
                    onCommandDone.run();
                } catch (Exception e) {
                    setError(GloryManager.Error.APP_ERROR, e.getMessage());
                }
            }
        }
    }

    protected void clearError(boolean publish) {
        threadCommandApi.clearError();
        if (publish) {
            if (onCommandDone != null) {
                try {
                    onCommandDone.run();
                } catch (Exception e) {
                    setError(GloryManager.Error.APP_ERROR, e.getMessage());
                }
            }
        }
    }
}
