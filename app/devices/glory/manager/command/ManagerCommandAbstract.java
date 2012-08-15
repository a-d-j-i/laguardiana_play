/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.GloryStatus.SR1Mode;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.Manager.ThreadCommandApi;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract implements Runnable {

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
    protected final ThreadCommandApi threadCommandApi;

    public ManagerCommandAbstract(ThreadCommandApi threadCommandApi) {
        this.threadCommandApi = threadCommandApi;
    }
    private AtomicBoolean isDone = new AtomicBoolean(false);
    private AtomicBoolean cancel = new AtomicBoolean(false);

    abstract public void execute();

    public void run() {
        isDone.set(false);
        threadCommandApi.setSuccess(null);
        threadCommandApi.setError(null);
        execute();
        isDone.set(true);
    }

    public boolean isDone() {
        return isDone.get();
    }

    public void cancel() {
        cancel.set(true);
    }

    public boolean mustCancel() {
        return (cancel.get() || threadCommandApi.mustStop());
    }

    boolean gotoNeutral(boolean openEscrow, boolean storingError) {
        Logger.debug("GOTO NEUTRAL");
        threadCommandApi.setError(null);
        if (!sense()) {
            return false;
        }
        switch (gloryStatus.getSr1Mode()) {
            case storing_error:
                if (!storingError) {
                    threadCommandApi.setError("Storing error must call admin");
                    return false;
                }
                switch (gloryStatus.getD1Mode()) {
                    case storing_error_recovery_mode:
                    case normal_error_recovery_mode:
                    case deposit:
                    case manual:
                        if (!remoteCancel()) {
                            return false;
                        }
                    // dont break;
                    case neutral:
                        storingErrorRecovery();
                        break;
                    default:
                        threadCommandApi.setError(String.format("gotoNeutral Abnormal device Invalid D1 mode %s", gloryStatus.getD1Mode().name()));
                        return false;
                }
                break;

            case abnormal_device:
                switch (gloryStatus.getD1Mode()) {
                    case deposit:
                    case manual:
                        if (!remoteCancel()) {
                            return false;
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
                        threadCommandApi.setError(String.format("gotoNeutral Abnormal device Invalid D1-2 mode %s", gloryStatus.getD1Mode().name()));
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
                    case storing_start_request:
                        if (!openEscrow) {
                            threadCommandApi.setError("There are bills in the escrow call an admin");
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
                        remoteCancel();
                        break;
                    case being_recover_from_storing_error:
                        if (storingError) {
                            errorRecovery();
                        } else {
                            WaitForEmptyEscrow();
                            threadCommandApi.setError("Storing error must call admin");
                            remoteCancel();
                        }
                        break;
                    case counting_start_request:
                        remoteCancel();
                        break;

                    case waiting:
                        remoteCancel();
                        break;
                    default:
                        threadCommandApi.setError(String.format("gotoNeutral Abnormal device Invalid D1-3 mode %s", gloryStatus.getD1Mode().name()));
                        break;
                }
                break;
            case initial:
                if (!remoteCancel()) {
                    return false;
                }
                if (gloryStatus.getD1Mode() != GloryStatus.D1Mode.neutral) {
                    threadCommandApi.setError(String.format("cant set neutral mode d1 (%s) mode not neutral", gloryStatus.getD1Mode().name()));
                }
                break;
            case neutral:
                break;
            default:
                threadCommandApi.setError(String.format("gotoNeutralInvalid D1-3 mode %s", gloryStatus.getD1Mode().name()));
                break;
        }
        threadCommandApi.setSuccess(null);
        Logger.debug("GOTO NEUTRAL DONE");
        return true;
    }

    boolean sendGloryCommand(GloryCommandAbstract cmd) {
        if (cmd != null) {
            if (!sendGCommand(cmd)) {
                return false;
            }
        }
        return sense();
    }

    boolean sense() {
        if (!sendGCommand(new devices.glory.command.Sense())) {
            return false;
        }
        Logger.debug(String.format("D1Mode %s SR1 Mode : %s", gloryStatus.getD1Mode().name(), gloryStatus.getSr1Mode().name()));
        return true;
    }

    boolean sendGCommand(GloryCommandAbstract cmd) {
        if (cmd == null) {
            threadCommandApi.setError("Invalid command null");
            return false;
        }
        if (!gloryStatus.setStatusOk(threadCommandApi.sendGloryCommand(cmd))) {
            String error = gloryStatus.getLastError();
            threadCommandApi.setError(error);
            return false;
        }
        return true;
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
        for (int i = 0; i < 0xffff; i++) {
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
                    threadCommandApi.setError("waitUntilSR1State Abnormal device, todo: get the flags");
                    return false;
                case storing_error:
                    threadCommandApi.setError("waitUntilSR1State Storing error, todo: get the flags");
                    return false;
                default:
                    break;
            }
            sleep();
        }
        threadCommandApi.setError(String.format("cant set sr1 mode to %s", state.name()));
        return false;
    }

    boolean waitUntilD1State(D1Mode state) {
        for (int i = 0; i < 0xffff; i++) {
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
                    threadCommandApi.setError("waitUntilD1State Abnormal device, todo: get the flags");
                    return false;
                case storing_error:
                    threadCommandApi.setError("waitUntilD1State Storing error, todo: get the flags");
                    return false;
                default:
                    break;
            }
            sleep();
        }
        threadCommandApi.setError(String.format("cant set d1 mode to %s", state.name()));
        return false;
    }

    void WaitForEmptyEscrow() {
        threadCommandApi.setSuccess("Remove the bills from the escrow");
        for (int i = 0; i < 0xffff; i++) {
            Logger.debug("WaitForEmptyEscrow");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case being_recover_from_storing_error:
                case escrow_close_request:
                case waiting_for_an_envelope_to_set:
                    threadCommandApi.setSuccess(null);
                    if (!sendGloryCommand(new devices.glory.command.CloseEscrow())) {
                        return;
                    }
                    break;
                case being_restoration:
                case escrow_open:
                case escrow_close:
                    break;
                case storing_start_request:
                case counting_start_request:
                case waiting:
                    return;
                case abnormal_device:
                    threadCommandApi.setError("Abnormal device, todo: get the flags");
                    return;
                case storing_error:
                    threadCommandApi.setError("Storing error, todo: get the flags");
                    return;
                default:
                    threadCommandApi.setError(String.format("WaitForEmptyEscrow invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    break;
            }
            sleep();
        }
        threadCommandApi.setError("WaitForEmptyEscrow waiting too much");
    }

    private boolean remoteCancel() {
        for (int i = 0; i < 0xffff; i++) {
            Logger.debug("remoteCancel");
            if (!sense()) {
                return false;
            }
            if (gloryStatus.isRejectBillPresent()) {
                threadCommandApi.setSuccess("Remove rejected bills");
            } else {
                if (gloryStatus.isHopperBillPresent()) {
                    threadCommandApi.setSuccess("Remove bills from the hoper");
                } else {
                    break;
                }
            }
            sleep();
        }
        threadCommandApi.setSuccess(null);
        return sendGloryCommand(new devices.glory.command.RemoteCancel());
    }

    void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }
}
