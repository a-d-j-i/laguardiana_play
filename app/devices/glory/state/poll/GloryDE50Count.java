package devices.glory.state.poll;

import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50Error;
import devices.glory.state.GloryDE50Error.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.COUNTING;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.JAM;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.PUT_THE_BILLS_ON_THE_HOPER;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_THE_BILLS_FROM_ESCROW;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_THE_BILLS_FROM_HOPER;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Count extends GloryDE50StatePoll {
    /* Those must be treated as different states because there can be a race condition.
     private final AtomicBoolean needToStoreDeposit = new AtomicBoolean(false);
     private final AtomicBoolean needToWithdrawDeposit = new AtomicBoolean(false);
     private final AtomicBoolean storeDepositDone = new AtomicBoolean(false);
     private final AtomicBoolean withdrawDepositDone = new AtomicBoolean(false);
     */

    private final AtomicReference<Map<Integer, Integer>> currentQuantity = new AtomicReference<Map<Integer, Integer>>();

    final private Map<String, Integer> desiredQuantity;
    final private Integer currency;

    private final int MAX_COUNT_RETRIES = 10;
    private int currentSlot;
    private final boolean isBatch;
    boolean fakeCount = false;
    int count_retries = 0;
    boolean batchEnd = false;
    boolean debug = true;

    public GloryDE50Count(GloryDE50Device api, Map<String, Integer> desiredQuantity, Integer currency) {
        super(api);
        this.desiredQuantity = desiredQuantity;
        if (currency == null) {
            this.currency = 0;
        } else {
            this.currency = currency;
        }
        int cSlot = 0;
        if (desiredQuantity == null) {
            isBatch = false;
            currentSlot = 0;
        } else {
            for (String k : desiredQuantity.keySet()) {
                Integer v = desiredQuantity.get(k);
                this.desiredQuantity.put(k, v);
                if (v == 0) {
                    cSlot++;
                }
            }
            isBatch = (cSlot < desiredQuantity.size());
            currentSlot = cSlot;
        }
    }

    @Override
    public GloryDE50StateAbstract init() {
        Logger.error("CountCommand Start CURRENCY %d", currency.byteValue());
        return this;
    }

    @Override
    public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse) {
        GloryDE50StateAbstract sret;
        Logger.debug("COUNT_COMMAND");
        if (lastResponse.isCassetteFullCounter()) {
            return new GloryDE50RotateCassete(api, this);
        }
        // If I'm not in deposit mode try to get there once
        switch (lastResponse.getD1Mode()) {
            case deposit:
                break;
            case neutral:
                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                } else {
                    Logger.error("CountCommand CURRENCY %d", currency.byteValue());
                    sret = sendGloryOperation(new devices.glory.operation.SwitchCurrency(currency.byteValue()));
                    if (sret != null) {
                        return sret;
                    }
                    GloryDE50OperationResponse response = new GloryDE50OperationResponse();
                    String error = api.sendGloryDE50Operation(new devices.glory.operation.SetDepositMode(), debug, response);
                    if (error == null) {
                        Logger.error("Error %s sending cmd : SetDepositMode", error);
                        return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
                    }
                    if (response.isCassetteFullCounter()) {
                        return new GloryDE50RotateCassete(api, this);
                    }
                }
                return this;
        }

        switch (lastResponse.getSr1Mode()) {
            case storing_start_request:
                // When there are a fake count but there are bills there I get directly a storing_start_request
                // Send the missing event here.
                if (fakeCount) {
                    fakeCount = false;
                    api.notifyListeners(COUNTING);
                    refreshQuantity();
                }
                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                /*  REVIEW
                 if (needToStoreDeposit.get()) {
                 // We clear the counter because they are invalid now
                 clearQuantity();
                 sret = sendGloryOperation(new devices.glory.operation.StoringStart(0));
                 if (sret != null) {
                 return sret;
                 }
                 notifyListeners(STORING);
                 break;
                 } else if (needToWithdrawDeposit.get()) {
                 api.setClosing(false);
                 sret = sendGloryOperation(new devices.glory.operation.OpenEscrow());
                 if (sret != null) {
                 return sret;
                 }
                 //                        countData.withdrawDepositDone();
                 break;
                 } else {
                 if (isBatch && batchEnd) {
                 return this;
                 }
                 // We need a valid counters before generating the events.
                 sret = refreshQuantity();
                 if (sret != null) {
                 return sret;
                 }
                 if (lastResponse.isEscrowFull()) {
                 notifyListeners(ESCROW_FULL);
                 break;
                 }
                 if (lastResponse.isHopperBillPresent()) {
                 sret = batchCountStart();
                 break;
                 }
                 notifyListeners(READY_TO_STORE);
                 }
                 */
                break;
            case escrow_open:
                api.notifyListeners(REMOVE_THE_BILLS_FROM_ESCROW);
                break;
            case escrow_close: // The escrow is closing... wait.
                api.setClosing(true);
                break;
            case being_restoration:
                break;
            case escrow_close_request:
                if (lastResponse.isEscrowBillPresent()) {
                    break;
                }
            // don't break
            case being_recover_from_storing_error:
            case waiting_for_an_envelope_to_set:
                sret = sendGloryOperation(new devices.glory.operation.CloseEscrow());
                if (sret != null) {
                    return sret;
                }
                api.setClosing(true);
                break;

            case counting:
                if (!fakeCount) {
                    api.notifyListeners(COUNTING);
                    // The second time after storing.
                    // Ignore error.
                    refreshQuantity();
                }
                break;
            case waiting:
                sret = refreshQuantity();
                if (sret != null) {
                    return sret;
                }
                if (!lastResponse.isHopperBillPresent()) {
                    api.notifyListeners(PUT_THE_BILLS_ON_THE_HOPER);
                }
                count_retries = 1;
                break;
            case being_store:
                fakeCount = true;
//                storeDepositDone.set(true);
                break;
            case counting_start_request:
                sret = refreshQuantity();
                if (sret != null) {
                    return sret;
                }

                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                if (isNoCounts()) {
                    if (count_retries > MAX_COUNT_RETRIES) {
                        Logger.error("Error in hopper sensor");
                        api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                        //setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "Error in hopper sensor"));
                        break;
                    } else {
                        count_retries++;
                    }
                } else {
                    count_retries = 1;
                }

                fakeCount = false;
                /*                withdrawDepositDone.set(true);
                 if (!needToStoreDeposit.get()) {
                 // If there are bills in the hoper then it comes here after storing a full escrow
                 if (isBatch && batchEnd) { //BATCH END
                 api.setClosing(false);
                 sret = sendGloryOperation(new devices.glory.operation.OpenEscrow());
                 if (sret != null) {
                 return sret;
                 }
                 return new GotoNeutral(api, this, true, true);
                 }
                 if (lastResponse.isRejectBillPresent()) {
                 notifyListeners(REMOVE_REJECTED_BILLS);
                 break;
                 }
                 sret = batchCountStart();
                 if (sret != null) { // batch end
                 return sret;
                 }
                 }
                 */
                break;
            case abnormal_device:
                api.notifyListeners(JAM);
                return new GloryDE50GotoNeutral(api, this, true, true);
            case storing_error:
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN, String.format("Count Storing error, todo: get the flags"));
            default:
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, String.format("Count invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
        }
        return this;
    }

    GloryDE50StateAbstract batchCountStart() {
        int[] bills = new int[32];

        // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        if (!isBatch) {
            /*if (!sendGloryCommand(new devices.glory.command.BatchDataTransmition(bills))) {
             return false;
             }*/
            // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
            GloryDE50OperationResponse response = new GloryDE50OperationResponse();
            String error = api.sendGloryDE50Operation(new devices.glory.operation.BatchDataTransmition(bills), debug, response);
            if (error == null) {
                api.notifyListeners(COUNTING);
            } else {
                api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
            }
            return this;
        }

        Logger.debug("ISBATCH");
        GloryDE50OperationResponse response = new GloryDE50OperationResponse();
        String error = api.sendGloryDE50Operation(new devices.glory.operation.CountingDataRequest(), debug, response);
        if (error != null) {
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
        }
        Map<Integer, Integer> currentQ = response.getBills();
        if (currentQ == null) {
            String err = String.format("Error getting current count");
            Logger.error("Error %s sending cmd : CountingDataRequest", err);
            return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
        }

        while (currentSlot < 32) {
            int desired = 0;
            if (desiredQuantity.get(Integer.toString(currentSlot)) != null) {
                desired = desiredQuantity.get(Integer.toString(currentSlot));
            }
            int current = currentQ.get(currentSlot);
            if (current > desired) {
                String err = String.format("Invalid bill value %d %d %d", currentSlot, current, desired);
                Logger.error("Error %s sending cmd : CountingDataRequest", err);
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
            }
            bills[ currentSlot] = desired - current;
            Logger.debug("---------- slot %s batch billls : %d desired %d value %d", currentSlot, bills[ currentSlot], desired, current);
            if (bills[ currentSlot] != 0) {
                break;
            } else {
                currentSlot++;
            }
        }
        if (currentSlot < 32) {
            GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.operation.BatchDataTransmition(bills));
            if (sret != null) {
                return sret;
            }
            api.notifyListeners(COUNTING);
            //? return false?
        }
        // TODO: recheck
        batchEnd = true;
        return this;
    }

    private GloryDE50StateAbstract refreshQuantity() {
        GloryDE50OperationResponse response = new GloryDE50OperationResponse();
        String error = api.sendGloryDE50Operation(new devices.glory.operation.CountingDataRequest(), debug, response);
        if (error != null) {
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
        }
        currentQuantity.set(response.getBills());
//        for (Integer k : bills.keySet()) {
//            Logger.debug("bill %d %d", k, bills.get(k));
//        }

        return null;
    }

    private boolean isNoCounts() {
        for (Integer i : currentQuantity.get().values()) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean clearQuantity() {
        currentQuantity.set(new HashMap<Integer, Integer>());
        return true;
    }

    public Integer getCurrency() {
        return currency;
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return currentQuantity.get();
    }

    public Map<String, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return sendGloryOperation(new devices.glory.operation.StopCounting());
    }

    public boolean storeDeposit(Integer sequenceNumber) {
//        needToStoreDeposit.set(true);
        return true;
    }

}
