package devices.glory.manager.command;

import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.GloryManagerError;
import devices.glory.manager.ManagerInterface;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class CountCommand extends ManagerCommandAbstract {

    private final int MAX_COUNT_RETRIES = 10;
    private final CountData countData;

    public CountCommand(ThreadCommandApi threadCommandApi, Map<Integer, Integer> desiredQuantity, Integer currency) {
        super(threadCommandApi);
        countData = new CountData(desiredQuantity, currency);
    }

    static public class CountData extends CommandData {

        private Map< Integer, Integer> currentQuantity = new HashMap<Integer, Integer>();
        private final Integer currency;
        private boolean withdrawDeposit = false;
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

        private boolean isNoCounts() {
            rlock();
            try {
                for (Integer i : currentQuantity.values()) {
                    if (i != 0) {
                        return false;
                    }
                }
                return true;
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

        private boolean needToWithdrawDeposit() {
            rlock();
            try {
                return withdrawDeposit;
            } finally {
                runlock();
            }
        }

        private void withdrawDeposit() {
            wlock();
            try {
                this.withdrawDeposit = true;
            } finally {
                wunlock();
            }
        }

        private void withdrawDepositDone() {
            wlock();
            try {
                this.withdrawDeposit = false;
            } finally {
                wunlock();
            }
        }
    }

    @Override
    public void run() {
        boolean fakeCount = false;
        int count_retries = 0;

        if (!gotoNeutral(false, false)) {
            return;
        }
        Logger.error("CountCommand Start CURRENCY %d", countData.currency.byteValue());
        boolean batchEnd = false;
        while (!mustCancel()) {
            //Logger.debug("Count Command Counting");
            if (!sense()) {
                return;
            }
            // If I'm not in deposit mode try to get there once
            switch (gloryStatus.getD1Mode()) {
                case deposit:
                    break;
                case neutral:
                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                    } else {
                        Logger.error("CountCommand CURRENCY %d", countData.currency.byteValue());
                        if (!sendGloryCommand(new devices.glory.command.SwitchCurrency(countData.currency.byteValue()))) {
                            return;
                        }
                        if (!sendGCommand(new devices.glory.command.SetDepositMode())) {
                            if (gloryStatus.isCassetteFullCounter()) {
                                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.CASSETE_FULL, "Cassete Full"));
                                return;
                            }
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                    String.format("CountCommand gotoDepositMode Error %s", gloryStatus.getLastError())));
                            return;
                        }
                    }
                    sleep();
                    continue;
            }

            if (gloryStatus.isCassetteFullCounter()) {
                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.CASSETE_FULL, "Cassete Full"));
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_start_request:
                    // Where there is a fake count but there are bills there I get directly a storing_start_request
                    // Send the missing event here.
                    if (fakeCount) {
                        fakeCount = false;
                        setState(ManagerInterface.MANAGER_STATE.COUNTING);
                        refreshQuantity();
                    }
                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                        break;
                    }
                    if (countData.needToStoreDeposit()) {
                        // We clear the counter because they are invalid now
                        clearQuantity();
                        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                            return;
                        }
                        setState(ManagerInterface.MANAGER_STATE.STORING);
                        break;
                    } else if (countData.needToWithdrawDeposit()) {
                        if (!openEscrow()) {
                            return;
                        }
//                        countData.withdrawDepositDone();
                        break;
                    } else {
                        if (countData.isBatch && batchEnd) {
                            sleep();
                            break;
                        }
                        // We need a valid counters before generating the events.
                        if (!refreshQuantity()) {
                            String error = gloryStatus.getLastError();
                            Logger.error("Error %s sending cmd : CountingDataRequest", error);
                            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
                            return;
                        }
                        if (gloryStatus.isEscrowFull()) {
                            setState(ManagerInterface.MANAGER_STATE.ESCROW_FULL);
                            break;
                        }
                        if (gloryStatus.isHopperBillPresent()) {
                            if (batchCountStart()) { // batch end
                                batchEnd = true;
                            }
                            break;
                        }
                        setState(ManagerInterface.MANAGER_STATE.READY_TO_STORE);
                    }
                    break;
                case escrow_open:
                    setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_ESCROW);
                    break;
                case escrow_close: // The escrow is closing... wait.
                    threadCommandApi.setClosing(true);
                    break;
                case being_restoration:
                    break;
                case escrow_close_request:
                    if (gloryStatus.isEscrowBillPresent()) {
                        break;
                    }
                // don't break
                case being_recover_from_storing_error:
                case waiting_for_an_envelope_to_set:
                    if (!closeEscrow()) {
                        return;
                    }
                    break;

                case counting:
                    if (!fakeCount) {
                        setState(ManagerInterface.MANAGER_STATE.COUNTING);
                        // The second time after storing.
                        // Ignore error.
                        refreshQuantity();
                    }
                    break;
                case waiting:
                    if (!refreshQuantity()) {
                        String error = gloryStatus.getLastError();
                        Logger.error("Error %s sending cmd : CouProcessJamntingDataRequest", error);
                        setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
                        return;
                    }
                    if (!gloryStatus.isHopperBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.PUT_THE_BILLS_ON_THE_HOPER);
                    }
                    count_retries = 1;
                    break;
                case being_store:
                    fakeCount = true;
                    countData.storeDepositDone();
                    break;
                case counting_start_request:
                    if (!refreshQuantity()) {
                        String error = gloryStatus.getLastError();
                        Logger.error("Error %s sending cmd : CouProcessJamntingDataRequest", error);
                        setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
                        return;
                    }

                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                        break;
                    }
                    if (countData.isNoCounts()) {
                        if (count_retries > MAX_COUNT_RETRIES) {
                            Logger.error("Error in hopper sensor");
                            setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER);
                            //setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "Error in hopper sensor"));
                            break;
                        } else {
                            count_retries++;
                        }
                    } else {
                        count_retries = 1;
                    }

                    fakeCount = false;
                    countData.withdrawDepositDone();
                    if (!countData.needToStoreDeposit()) {
                        // If there are bills in the hoper then it comes here after storing a full escrow
                        if (countData.isBatch && batchEnd) { //BATCH END
                            if (!openEscrow()) {
                                return;
                            }
                            gotoNeutral(true, true);
                            return;
                        }
                        if (gloryStatus.isRejectBillPresent()) {
                            setState(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS);
                            break;
                        }
                        if (batchCountStart()) { // batch end
                            batchEnd = true;
                        }
                    }
                    break;
                case abnormal_device:
                    setState(ManagerInterface.MANAGER_STATE.JAM);
                    if (!gotoNeutral(true, true)) {
                        return;
                    }
                    if (!sendGCommand(new devices.glory.command.SetDepositMode())) {
                        setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                                String.format("CountCommand gotoDepositMode Error %s", gloryStatus.getLastError())));
                        return;
                    }
                    break;
                case storing_error:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.STORING_ERROR_CALL_ADMIN,
                            String.format("Count Storing error, todo: get the flags")));
                    return;
                default:
                    setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                            String.format("Count invalid sr1 mode %s", gloryStatus.getSr1Mode().name())));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            if (!sendGloryCommand(new devices.glory.command.StopCounting())) {
                return;
            }
            setState(ManagerInterface.MANAGER_STATE.CANCELING);
        }
        gotoNeutral(true, false);
    }

    public void storeDeposit(Integer sequenceNumber) {
        countData.storeDeposit();
    }

    public void withdrawDeposit() {
        countData.withdrawDeposit();
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

        // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        if (!countData.isBatch) {
            /*if (!sendGloryCommand(new devices.glory.command.BatchDataTransmition(bills))) {
             return false;
             }*/
            // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
            GloryCommandAbstract cmd = new devices.glory.command.BatchDataTransmition(bills);
            if (sendGCommand(cmd)) {
                if (sense()) {
                    setState(ManagerInterface.MANAGER_STATE.COUNTING);
                    return true;
                }
            } else {
                setState(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER);
            }
            return false;
        }

        Logger.debug("ISBATCH");
        if (!sendGCommand(new devices.glory.command.CountingDataRequest())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, error));
            return false;
        }
        Map<Integer, Integer> currentQuantity = gloryStatus.getBills();
        if (currentQuantity == null) {
            setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                    String.format("Error getting current count")));
            return false;
        }

        while (countData.currentSlot < 32) {
            int desired = 0;
            if (countData.desiredQuantity.get(countData.currentSlot) != null) {
                desired = countData.desiredQuantity.get(countData.currentSlot).intValue();
            }
            int current = currentQuantity.get(countData.currentSlot);
            if (current > desired) {
                setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR,
                        String.format("Invalid bill value %d %d %d", countData.currentSlot, current, desired)));
                return false;
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
                return false;
            }
            setState(ManagerInterface.MANAGER_STATE.COUNTING);
            return false;
        }
        return true;
    }

    private boolean refreshQuantity() {
        if (!sendGCommand(new devices.glory.command.CountingDataRequest())) {
            return false;
        }
        Map<Integer, Integer> bills = gloryStatus.getBills();
//        for (Integer k : bills.keySet()) {
//            Logger.debug("bill %d %d", k, bills.get(k));
//        }
        countData.setCurrentQuantity(bills);
        return true;
    }

    private boolean clearQuantity() {
        countData.setCurrentQuantity(new HashMap<Integer, Integer>());
        return true;
    }
}
