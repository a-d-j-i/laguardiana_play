package devices.glory.manager.command;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.ManagerInterface;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class CountCommand extends ManagerCommandAbstract {

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
        if (!gotoNeutral(false, false)) {
            return;
        }
        Logger.error("CURRENCY %d", countData.currency.byteValue());
        if (!sendGloryCommand(new devices.glory.command.SwitchCurrency(countData.currency.byteValue()))) {
            return;
        }
        if (!sendGCommand(new devices.glory.command.SetDepositMode())) {
            setError(ManagerInterface.Error.APP_ERROR,
                    String.format("CountCommand gotoDepositMode Error %s", gloryStatus.getLastError()));
            return;
        }
        boolean batchEnd = false;
        while (!mustCancel()) {
            Logger.debug("Count Command Counting");
            if (!sense()) {
                return;
            }
            switch (gloryStatus.getSr1Mode()) {
                case storing_start_request:
                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
                        break;
                    }
                    if (countData.needToStoreDeposit()) {
                        if (!refreshQuantity()) {
                            String error = gloryStatus.getLastError();
                            Logger.error("Error %s sending cmd : CountingDataRequest", error);
                            setError(ManagerInterface.Error.APP_ERROR, error);
                            return;
                        }
                        if (!sendGloryCommand(new devices.glory.command.StoringStart(0))) {
                            return;
                        }
                        setState(ManagerInterface.State.STORING);
                        break;
                    } else if (countData.needToWithdrawDeposit()) {
                        if (!sendGCommand(new devices.glory.command.OpenEscrow())) {
                            return;
                        }
//                        countData.withdrawDepositDone();
                        break;
                    } else {
                        if (countData.isBatch && batchEnd) {
                            sleep();
                            break;
                        }
                        if (gloryStatus.isEscrowFull()) {
                            setState(ManagerInterface.State.ESCROW_FULL);
                            break;
                        }
                        if (gloryStatus.isHopperBillPresent()) {
                            if (batchCountStart()) { // batch end
                                batchEnd = true;
                            }
                            break;
                        }
                        setState(ManagerInterface.State.READY_TO_STORE);
                    }
                    if (!refreshQuantity()) {
                        String error = gloryStatus.getLastError();
                        Logger.error("Error %s sending cmd : CountingDataRequest", error);
                        setError(ManagerInterface.Error.APP_ERROR, error);
                        return;
                    }
                    break;
                case escrow_open:
                    setState(ManagerInterface.State.REMOVE_THE_BILLS_FROM_ESCROW);
                    break;
                case escrow_close: // The escrow is closing... wait.
                case being_restoration:
                    break;
                case escrow_close_request:
                    if (gloryStatus.isEscrowBillPresent()) {
                        break;
                    }
                // don't break
                case being_recover_from_storing_error:
                case waiting_for_an_envelope_to_set:
                    if (!sendGloryCommand(new devices.glory.command.CloseEscrow())) {
                        return;
                    }
                    break;

                case counting:
                    setState(ManagerInterface.State.COUNTING);
                    // The second time after storing.
                    // Ignore error.
                    refreshQuantity();
                    break;
                case waiting:
                    if (!refreshQuantity()) {
                        String error = gloryStatus.getLastError();
                        Logger.error("Error %s sending cmd : CouProcessJamntingDataRequest", error);
                        setError(ManagerInterface.Error.APP_ERROR, error);
                        return;
                    }
                    if (!gloryStatus.isHopperBillPresent()) {
                        setState(ManagerInterface.State.PUT_THE_BILLS_ON_THE_HOPER);
                    }
                    break;
                case being_store:
                    countData.storeDepositDone();
                    break;
                case counting_start_request:
                    countData.withdrawDepositDone();
                    if (!countData.needToStoreDeposit()) {
                        // If there are bills in the hoper then it comes here after storing a full escrow
                        if (countData.isBatch && batchEnd) { //BATCH END
                            if (!sendGloryCommand(new devices.glory.command.OpenEscrow())) {
                                return;
                            }
                            gotoNeutral(true, true);
                            return;
                        }
                        if (gloryStatus.isRejectBillPresent()) {
                            setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
                            break;
                        }
                        if (batchCountStart()) { // batch end
                            batchEnd = true;
                        }
                    }
                    if (gloryStatus.isRejectBillPresent()) {
                        setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
                        break;
                    }
                    break;
                case abnormal_device:
                    setState(ManagerInterface.State.JAM);
                    if (!gotoNeutral(true, true)) {
                        return;
                    }
                    if (!sendGCommand(new devices.glory.command.SetDepositMode())) {
                        setError(ManagerInterface.Error.APP_ERROR,
                                String.format("CountCommand gotoDepositMode Error %s", gloryStatus.getLastError()));
                        return;
                    }
                    break;
                case storing_error:
                    setError(ManagerInterface.Error.STORING_ERROR_CALL_ADMIN,
                            String.format("Count Storing error, todo: get the flags"));
                    return;
                default:
                    setError(ManagerInterface.Error.APP_ERROR,
                            String.format("Count invalid sr1 mode %s", gloryStatus.getSr1Mode().name()));
                    return;
            }
            sleep();
        }
        if (mustCancel()) {
            if (!sendGloryCommand(new devices.glory.command.StopCounting())) {
                return;
            }
            setState(ManagerInterface.State.CANCELING);
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

        if (!countData.isBatch) {
            if (!sendGloryCommand(new devices.glory.command.BatchDataTransmition(bills))) {
                return false;
            };
            setState(ManagerInterface.State.COUNTING);
            return true;
        }

        Logger.debug("ISBATCH");
        if (!sendGCommand(new devices.glory.command.CountingDataRequest())) {
            String error = gloryStatus.getLastError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            setError(ManagerInterface.Error.APP_ERROR, error);
            return false;
        }
        Map<Integer, Integer> currentQuantity = gloryStatus.getBills();
        if (currentQuantity == null) {
            setError(ManagerInterface.Error.APP_ERROR,
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
                setError(ManagerInterface.Error.APP_ERROR,
                        String.format("Invalid bill value %d %d %d", countData.currentSlot, current, desired));
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
            setState(ManagerInterface.State.COUNTING);
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
}
