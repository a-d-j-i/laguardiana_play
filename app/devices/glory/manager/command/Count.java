/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.ThreadCommandApi;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class Count extends ManagerCommandAbstract {

    private final CountData countData;

    public Count(ThreadCommandApi threadCommandApi, Runnable onCommandDone, Map<Integer, Integer> desiredQuantity, Integer currency) {
        super(threadCommandApi, onCommandDone);
        countData = new CountData(desiredQuantity, currency);
    }

    static public class CountData extends CommandData {

        private Map< Integer, Integer> currentQuantity = new HashMap<Integer, Integer>();
        private final Integer currency;
        private boolean storeDeposit = false;
        private final Map<Integer, Integer> desiredQuantity = new HashMap<Integer, Integer>();
        private int currentSlot = 0;
        private final boolean isBatch;

        public CountData(Map<Integer, Integer> desiredQuantity, Integer currency) {
            if (currency == null) {
                this.currency = 0;
            } else {
                this.currency = currency;
            }
            currentSlot = 0;
            if (desiredQuantity == null) {
                isBatch = false;
                return;
            }
            for (Integer k : desiredQuantity.keySet()) {
                Integer v = desiredQuantity.get(k);
                this.desiredQuantity.put(k, v);
                if (v == 0) {
                    currentSlot++;
                }
            }
            isBatch = (currentSlot < desiredQuantity.size());
        }

        private Integer getCurrency() {
            return currency;
        }

        private Map< Integer, Integer> getCurrentQuantity() {
            rlock();
            try {
                return currentQuantity;
            } finally {
                runlock();
            }
        }

        private void setCurrentQuantity(Map<Integer, Integer> billData) {
            wlock();
            try {
                this.currentQuantity = billData;
            } finally {
                wunlock();
            }
        }

        private boolean needToStoreDeposit() {
            rlock();
            try {
                return storeDeposit;
            } finally {
                runlock();
            }
        }

        private void storeDeposit() {
            wlock();
            try {
                this.storeDeposit = true;
            } finally {
                wunlock();
            }
        }

        private void storeDepositDone() {
            wlock();
            try {
                this.storeDeposit = false;
            } finally {
                wunlock();
            }
        }
    }

    @Override
    public void execute() {
        threadCommandApi.setStatus(Manager.Status.IDLE);

        boolean batchEnd = false;
        if (!gotoNeutral(false, false)) {
            // ERROR.
            return;
        }
        Logger.error("CURRENCY %d", countData.currency.byteValue());
        if (!sendGloryCommand(new devices.glory.command.SwitchCurrency(countData.currency.byteValue()))) {
            return;
        }
        if (!sendGloryCommand(new devices.glory.command.SetDepositMode())) {
            return;
        }
        if (!waitUntilD1State(GloryStatus.D1Mode.deposit)) {
            return;
        }
        threadCommandApi.setStatus(Manager.Status.PUT_THE_BILLS_ON_THE_HOPER);
        boolean storeTry = false;
        while (!mustCancel()) {
            Logger.debug("Counting");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_start_request:
                    if (countData.needToStoreDeposit()) {
                        countData.storeDepositDone();
                        threadCommandApi.setStatus(Manager.Status.STORING);
                        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                            return;
                        }
                        break;
                    } else {
                        if (countData.isBatch && batchEnd) {
                            sleep();
                            break;
                        }
                        if (gloryStatus.isEscrowFull()) {
                            threadCommandApi.setStatus(Manager.Status.ESCROW_FULL);
                            break;
                        }
                        if (gloryStatus.isHopperBillPresent()) {
                            if (batchCountStart()) { // batch end
                                batchEnd = true;
                            }
                            break;
                        }
                        threadCommandApi.setStatus(Manager.Status.READY_TO_STORE);
                    }
                    if (!refreshCurrentQuantity()) {
                        return;
                    }
                    break;
                case counting:
                    // The second time after storing.
                    if (!refreshCurrentQuantity()) {
                        return;
                    }
                    break;
                case waiting:
                    // The second time after storing.
                    if (storeTry) {
                        gotoNeutral(true, false);
                        threadCommandApi.setStatus(Manager.Status.IDLE);
                        return;
                    }
                    if (!refreshCurrentQuantity()) {
                        return;
                    }
                    break;
                case being_store:
                    storeTry = true;
                    break;

                case counting_start_request:
                    // If there are bills in the hoper then it comes here after storing a full escrow
                    if (countData.isBatch && batchEnd) { //BATCH END
                        if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                            return;
                        }
                        WaitForEmptyEscrow();
                        gotoNeutral(true, false);
                        threadCommandApi.setStatus(Manager.Status.IDLE);
                        return;
                    }
                    if (storeTry) {
                        // TODO: Review this !!!
                        // The command must end or not ???
                        if (onCommandDone != null) {
                            try {
                                onCommandDone.run();
                            } catch (Exception e) {
                                threadCommandApi.setError(Manager.Error.APP_ERROR, e.getMessage());
                            }
                        }
                        //gotoNeutral(true, false);
                        threadCommandApi.setStatus(Manager.Status.IDLE);
                    }
                    if (batchCountStart()) { // batch end
                        batchEnd = true;
                    }
                    break;
                case abnormal_device:
                    threadCommandApi.setError(Manager.Error.JAM,
                            String.format("Count Abnormal device, todo: get the flags"));
                    return;
                case storing_error:
                    threadCommandApi.setError(Manager.Error.STORING_ERROR_CALL_ADMIN,
                            String.format("Count Storing error, todo: get the flags"));
                    return;
                default:
                    threadCommandApi.setError(Manager.Error.APP_ERROR,
                            String.format("Count invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            threadCommandApi.setStatus(Manager.Status.CANCELING);
        }
        gotoNeutral(true, false);
    }

    public void storeDeposit(int sequenceNumber) {
        countData.storeDeposit();
    }

    public Integer getCurrency() {
        return countData.getCurrency();
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return countData.getCurrentQuantity();
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return countData.desiredQuantity;
    }

    boolean batchCountStart() {
        int[] bills = new int[32];

        if (!countData.isBatch) {
            sendGloryCommand(new devices.glory.command.BatchDataTransmition(bills));
            return true;
        }

        Logger.debug("ISBATCH");
        if (!sendGCommand(new devices.glory.command.CountingDataRequest())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            threadCommandApi.setError(Manager.Error.APP_ERROR, error);
            return false;
        }
        Map<Integer, Integer> currentQuantity = gloryStatus.getBills();
        if (currentQuantity == null) {
            threadCommandApi.setError(Manager.Error.APP_ERROR,
                    String.format("Error getting current count"));
            return false;
        }

        while (countData.currentSlot < 32) {
            int desired = 0;
            if (countData.desiredQuantity.get(countData.currentSlot) != null) {
                desired = countData.desiredQuantity.get(countData.currentSlot).intValue();
            }
            int current = currentQuantity.get(countData.currentSlot);
            if (current > desired) {
                threadCommandApi.setError(Manager.Error.APP_ERROR,
                        String.format("Invalid bill value %d %d %d", countData.currentSlot, current, desired));
                return true;
            }
            bills[ countData.currentSlot] = desired - current;
            Logger.debug("---------- slot %d batch billls : %d desired %d value %d", countData.currentSlot, bills[ countData.currentSlot], desired, current);
            if (bills[ countData.currentSlot] != 0) {
                break;
            } else {
                countData.currentSlot++;
            }
        }
        if (countData.currentSlot < 32) {
            if (!sendGloryCommand(new devices.glory.command.BatchDataTransmition(bills))) {
                String error = gloryStatus.getLastError();
                Logger.error("Error %s sending cmd : BatchDataTransmition", error);
                threadCommandApi.setError(Manager.Error.APP_ERROR, error);
            }
            return false;
        }
        return true;
    }

    private boolean refreshCurrentQuantity() {
        if (!sendGCommand(new devices.glory.command.CountingDataRequest())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            threadCommandApi.setError(Manager.Error.APP_ERROR, error);
            return false;
        }
        Map<Integer, Integer> bills = gloryStatus.getBills();
//        for (Integer k : bills.keySet()) {
//            Logger.debug("bill %d %d", k, bills.get(k));
//        }
        countData.setCurrentQuantity(bills);
        return true;
    }

    @Override
    boolean sense() {
        if (super.sense()) {
            Map<Integer, Integer> bills = gloryStatus.getBills();
            if (bills != null && !bills.isEmpty()) {
                countData.setCurrentQuantity(bills);
            }
            return true;
        }
        return false;
    }
}
