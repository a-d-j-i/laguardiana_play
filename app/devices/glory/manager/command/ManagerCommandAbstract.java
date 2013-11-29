/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryState;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.GloryManagerError;
import devices.glory.manager.ManagerInterface;
import java.util.Date;
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
    protected final GloryState gloryStatus = new GloryState();
    protected final ThreadCommandApi threadCommandApi;

    public ManagerCommandAbstract(ThreadCommandApi threadCommandApi) {
        this.threadCommandApi = threadCommandApi;
    }
    private AtomicBoolean mustCancel = new AtomicBoolean(false);

    public void cancel() {
        mustCancel.set(true);
    }

    public boolean mustCancel() {
        return (mustCancel.get() || threadCommandApi.mustStop());
    }

    boolean gotoNeutral(boolean canOpenEscrow, boolean forceEmptyHoper) {
        boolean bagRotated = false;
        for (int i = 0; i < retries; i++) {
            Logger.debug("GOTO NEUTRAL %s %s",
                    (canOpenEscrow ? "OPEN ESCROW" : ""),
                    (forceEmptyHoper ? "FORCE EMPTY HOPER" : ""));

            // If I can open the escrow then I must wait untill it is empty
            if (mustCancel()) {
                Logger.debug("GOTO NEUTRAL MUST CANCEL");
            }
            if (mustCancel() && !canOpenEscrow) {
                Logger.debug("GOTO NEUTRAL CANCELED...");
                break;
            }
            if (!sense()) {
                return false;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_error:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error must call admin"));
                    return false;
            }
            if (gloryStatus.isCassetteFullCounter()) {
                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.CASSETE_FULL, "Cassete Full"));
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
                            if (threadCommandApi.isClosing()) {
                                /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                                 "Escrow door jamed"));
                                 return false;*/
                                setState(ManagerInterface.MANAGER_STATE.JAM);
                                break;
                            }
                            /*                            if (!canOpenEscrow) {
                             setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                             "There are bills in the escrow call an admin 1"));
                             return false;
                             }*/
                            if (!openEscrow()) {
                                break;
                            }
                            break;
                        case abnormal_device:
                            threadCommandApi.setClosing(false);
                            setState(ManagerInterface.MANAGER_STATE.JAM);
                            if (gloryStatus.getD1Mode() == GloryState.D1Mode.normal_error_recovery_mode) {
                                resetDevice();
                            } else {
                                if (!sendGCommand(new devices.glory.command.RemoteCancel())) {
                                    Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastError());
                                    return false;
                                }
                            }
                            break;
                        case escrow_close_request:
                        case being_recover_from_storing_error:
                            if (threadCommandApi.isClosing()) {
                                Logger.debug("--------->ISCLOSING");
                                /*setError(new GloryManagerError(GloryManagerError.ERROR_CODE.ESCROW_DOOR_JAMED,
                                 "Escrow door jamed"));
                                 return false;*/
                                setState(ManagerInterface.MANAGER_STATE.JAM);
                                break;
                            }
                            if (gloryStatus.isEscrowBillPresent()) {
                                break;
                            }
                        // don't break
                        case waiting_for_an_envelope_to_set:
                            if (!closeEscrow()) {
                                return false;
                            }
                            break;
                        case being_reset:
                            break;
                        case escrow_close: // The escrow is closing... wait.
                            threadCommandApi.setClosing(true);
                            break;
                        case counting: // Japaneese hack...
                            break;
                        case being_restoration:
                            setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_ESCROW);
                            break;
                        case escrow_open:
                            setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_ESCROW);
                            threadCommandApi.setClosing(false);
                            break;
                        case storing_error:
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error, todo: get the flags"));
                            return false;
                        case storing_start_request:
                            if (!canOpenEscrow) {
                                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.BILLS_IN_ESCROW_CALL_ADMIN, "There are bills in the escrow call an admin 2"));
                                return false;
                            }
                            if (!openEscrow()) {
                                break;
                            }
                            break;
                        case counting_start_request:
                        case being_exchange_the_cassette:
                        case waiting:
                            if (gloryStatus.isRejectBillPresent()) {
                                setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                                break;
                            }
                            if (gloryStatus.isHopperBillPresent()) {
                                setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER);
                                break;
                            }
                            if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                                return false;
                            }
                            break;
                        default:
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-1 mode %s", gloryStatus.getSr1Mode().name())));
                            break;
                    }
                    break;
                case initial:
                    if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                        return false;
                    }
                    break;
                case neutral:
                    switch (gloryStatus.getSr1Mode()) {
                        case abnormal_device:
                            setState(ManagerInterface.MANAGER_STATE.JAM);
                            if (!sendGCommand(new devices.glory.command.SetErrorRecoveryMode())) {
                                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                        String.format("gotoNeutral Error setting normal error recovery mode Error %s", gloryStatus.getLastError())));
                                return false;
                            }
                            break;
                        case waiting:
                            if (forceEmptyHoper) {
                                if (gloryStatus.isRejectBillPresent()) {
                                    setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                                    break;
                                } else if (gloryStatus.isHopperBillPresent()) {
                                    setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER);
                                    break;
                                }
                            }
                            if (gloryStatus.isEscrowBillPresent()) {
                                if (!canOpenEscrow) {
                                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.BILLS_IN_ESCROW_CALL_ADMIN, "There are bills in the escrow call an admin 3"));
                                    return false;
                                }
                                if (!openEscrow()) {
                                    break;
                                }
                            }
                            if (!bagRotated) {
                                // Rotate the bag once to fix the glory proble.
                                bagRotated = true;
                                // set the time if possible, some times it fails, ignroe this
                                if (!sendGCommand(new devices.glory.command.SetTime(new Date()))) {
                                    String error = gloryStatus.getLastError();
                                    Logger.error("Error %s sending cmd SetTime", error);
                                    break;
                                }
                                // Rotate if possible, some time it fails, ignore this
                                if (!sendGCommand(new devices.glory.command.SetCollectMode())) {
                                    String error = gloryStatus.getLastError();
                                    Logger.error("Error %s sending cmd SetCollectMode", error);
                                    break;
                                }
                                break;
                            }
                            setState(ManagerInterface.MANAGER_STATE.NEUTRAL);
                            Logger.debug("GOTO NEUTRAL DONE");
                            return true;
                        default:
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                    String.format("gotoNeutral Abnormal device Invalid SR1-2 mode %s", gloryStatus.getSr1Mode().name())));
                            break;
                    }
                    break;
                default:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                            String.format("gotoNeutralInvalid D1-4 mode %s", gloryStatus.getD1Mode().name())));
                    break;
            }
            sleep();
        }
        if (!mustCancel()) {
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "GOTO NEUTRAL TIMEOUT"));
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
                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
                return false;
            }
        }
        return sense();
    }

    boolean sense() {
        if (!sendGCommand(new devices.glory.command.Sense())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : SENSE", error);
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
            return false;
        }
        Logger.debug(String.format("Sense D1Mode %s SR1 Mode : %s", gloryStatus.getD1Mode().name(), gloryStatus.getSr1Mode().name()));
        return true;
    }

    boolean sendGCommand(GloryCommandAbstract cmd) {
        if (cmd == null) {
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "Invalid command null"));
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

    /*protected boolean sendRemoteCancel() {
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
     }*/
    void sleep() {
        sleep(500);
    }

    void sleep(int timems) {
        try {
            Thread.sleep(timems);
        } catch (InterruptedException ex) {
        }
    }

    protected void setError(GloryManagerError e) {
        Logger.error("MANAGER ERROR : %s", e);
        threadCommandApi.setError(e);
        setState(ManagerInterface.MANAGER_STATE.ERROR);
    }

    protected void setState(ManagerInterface.MANAGER_STATE state) {
        threadCommandApi.setState(state);
    }

    protected void clearError() {
        threadCommandApi.clearError();
    }

    protected boolean closeEscrow() {
        if (!sendGloryCommand(new devices.glory.command.CloseEscrow())) {
            return false;
        }
        threadCommandApi.setClosing(true);
        return true;
    }

    protected boolean openEscrow() {
        threadCommandApi.setClosing(false);
        return sendGCommand(new devices.glory.command.OpenEscrow());
    }
}
