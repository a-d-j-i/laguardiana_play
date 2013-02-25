/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.GloryManagerError;
import devices.glory.manager.ManagerInterface;
import java.util.Date;
import play.Logger;

/**
 *
 * @author adji
 */
public class CollectCommand extends ManagerCommandAbstract {

    public CollectCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void run() {
        // Set the machine time.
        for (int i = 0; i < retries; i++) {
            Logger.debug("COLLECT");

            if (!sense()) {
                return;
            }

            if (!gloryStatus.isCassetteFullCounter()) {
                sendGloryCommand(new devices.glory.command.SetTime(new Date()));
                clearError();
                setState(ManagerInterface.MANAGER_STATE.BAG_COLLECTED);
                Logger.debug("COLLECT DONE");
                return;
            }
            // If I can open the escrow then I must wait untill it is empty
            if (mustCancel()) {
                Logger.debug("COLLECT CANCELED...");
                break;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_error:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN, "Storing error must call admin"));
                    return;
            }
            switch (gloryStatus.getD1Mode()) {
                case collect_mode:
                case normal_error_recovery_mode:
                case storing_error_recovery_mode:
                case deposit:
                case manual:
                case initial:
                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                        break;
                    }
                    if (gloryStatus.isHopperBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER);
                        break;
                    }
                    if (!sendGloryCommand(new devices.glory.command.RemoteCancel())) {
                    }
                    break;
                case neutral:
                    if (gloryStatus.isCassetteFullCounter()) {
                        if (!sendGloryCommand(new devices.glory.command.SetCollectMode())) {
                            return;
                        }
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
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "COLLECT TIMEOUT"));
            Logger.debug("COLLECT TIMEOUT!!!");
        }

        Logger.debug("COLLECT DONE CANCEL");
    }
}
