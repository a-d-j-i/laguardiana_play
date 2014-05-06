package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import static devices.glory.GloryDE50Device.STATUS.COUNTING;
import static devices.glory.GloryDE50Device.STATUS.ESCROW_FULL;
import static devices.glory.GloryDE50Device.STATUS.JAM;
import static devices.glory.GloryDE50Device.STATUS.PUT_THE_BILLS_ON_THE_HOPER;
import static devices.glory.GloryDE50Device.STATUS.READY_TO_STORE;
import static devices.glory.GloryDE50Device.STATUS.REMOVE_REJECTED_BILLS;
import static devices.glory.GloryDE50Device.STATUS.REMOVE_THE_BILLS_FROM_ESCROW;
import static devices.glory.GloryDE50Device.STATUS.REMOVE_THE_BILLS_FROM_HOPER;
import static devices.glory.GloryDE50Device.STATUS.STORING;
import devices.glory.response.GloryDE50Response;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import play.Logger;

/**
 *
 * @author adji
 */
public class Count extends GloryDE50StatePoll {

    final private Map<Integer, Integer> desiredQuantity;
    final private Integer currency;

    private final int MAX_COUNT_RETRIES = 10;
    private int currentSlot;
    private final boolean isBatch;
    boolean fakeCount = false;
    int count_retries = 0;
    boolean batchEnd = false;
    private final AtomicBoolean needToStoreDeposit = new AtomicBoolean(false);
    private final AtomicBoolean needToWithdrawDeposit = new AtomicBoolean(false);
    private final AtomicBoolean storeDepositDone = new AtomicBoolean(false);
    private final AtomicBoolean withdrawDepositDone = new AtomicBoolean(false);
    private Map<Integer, Integer> currentQuantity;

    public Count(GloryDE50StateMachineApi api, Map<Integer, Integer> desiredQuantity, Integer currency) {
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
            for (Integer k : desiredQuantity.keySet()) {
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
    public GloryDE50StateAbstract poll(GloryDE50Response lastResponse) {
        GloryDE50StateAbstract sret;
        Logger.debug("COUNT_COMMAND");
        if (lastResponse.isCassetteFullCounter()) {
            return new RotateCassete(api, this);
        }
        // If I'm not in deposit mode try to get there once
        switch (lastResponse.getD1Mode()) {
            case deposit:
                break;
            case neutral:
                if (lastResponse.isRejectBillPresent()) {
                    notifyListeners(REMOVE_REJECTED_BILLS);
                } else {
                    Logger.error("CountCommand CURRENCY %d", currency.byteValue());
                    sret = sendGloryOperation(new devices.glory.command.SwitchCurrency(currency.byteValue()));
                    if (sret != null) {
                        return sret;
                    }
                    GloryDE50Response response = api.sendGloryOperation(new devices.glory.command.SetDepositMode());
                    if (response.isError()) {
                        if (response.isCassetteFullCounter()) {
                            return new RotateCassete(api, this);
                        }
                        String error = response.getError();
                        Logger.error("Error %s sending cmd : SetDepositMode", error);
                        return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
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
                    notifyListeners(COUNTING);
                    refreshQuantity();
                }
                if (lastResponse.isRejectBillPresent()) {
                    notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                if (needToStoreDeposit.get()) {
                    // We clear the counter because they are invalid now
                    clearQuantity();
                    sret = sendGloryOperation(new devices.glory.command.StoringStart(0));
                    if (sret != null) {
                        return sret;
                    }
                    notifyListeners(STORING);
                    break;
                } else if (needToWithdrawDeposit.get()) {
                    api.setClosing(false);
                    sret = sendGloryOperation(new devices.glory.command.OpenEscrow());
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
                break;
            case escrow_open:
                notifyListeners(REMOVE_THE_BILLS_FROM_ESCROW);
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
                sret = sendGloryOperation(new devices.glory.command.CloseEscrow());
                if (sret != null) {
                    return sret;
                }
                api.setClosing(true);
                break;

            case counting:
                if (!fakeCount) {
                    notifyListeners(COUNTING);
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
                    notifyListeners(PUT_THE_BILLS_ON_THE_HOPER);
                }
                count_retries = 1;
                break;
            case being_store:
                fakeCount = true;
                storeDepositDone.set(true);
                break;
            case counting_start_request:
                sret = refreshQuantity();
                if (sret != null) {
                    return sret;
                }

                if (lastResponse.isRejectBillPresent()) {
                    notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                if (isNoCounts()) {
                    if (count_retries > MAX_COUNT_RETRIES) {
                        Logger.error("Error in hopper sensor");
                        notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                        //setError(new GloryManagerError(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "Error in hopper sensor"));
                        break;
                    } else {
                        count_retries++;
                    }
                } else {
                    count_retries = 1;
                }

                fakeCount = false;
                withdrawDepositDone.set(true);
                if (!needToStoreDeposit.get()) {
                    // If there are bills in the hoper then it comes here after storing a full escrow
                    if (isBatch && batchEnd) { //BATCH END
                        api.setClosing(false);
                        sret = sendGloryOperation(new devices.glory.command.OpenEscrow());
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
                break;
            case abnormal_device:
                notifyListeners(JAM);
                return new GotoNeutral(api, this, true, true);
            case storing_error:
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.STORING_ERROR_CALL_ADMIN, String.format("Count Storing error, todo: get the flags"));
            default:
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, String.format("Count invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
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
            GloryDE50Response response = api.sendGloryOperation(new devices.glory.command.BatchDataTransmition(bills));
            if (!response.isError()) {
                notifyListeners(COUNTING);
                return this;
            } else {
                notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
            }
            return this;
        }

        Logger.debug("ISBATCH");
        GloryDE50Response response = api.sendGloryOperation(new devices.glory.command.CountingDataRequest());
        if (response.isError()) {
            String error = response.getError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
        }
        Map<Integer, Integer> currentQuantity = response.getBills();
        if (currentQuantity == null) {
            String error = String.format("Error getting current count");
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
        }

        while (currentSlot < 32) {
            int desired = 0;
            if (desiredQuantity.get(currentSlot) != null) {
                desired = desiredQuantity.get(currentSlot).intValue();
            }
            int current = currentQuantity.get(currentSlot);
            if (current > desired) {
                String error = String.format("Invalid bill value %d %d %d", currentSlot, current, desired);
                Logger.error("Error %s sending cmd : CountingDataRequest", error);
                return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
            }
            bills[ currentSlot] = desired - current;
            Logger.debug("---------- slot %d batch billls : %d desired %d value %d", currentSlot, bills[ currentSlot], desired, current);
            if (bills[ currentSlot] != 0) {
                break;
            } else {
                currentSlot++;
            }
        }
        if (currentSlot < 32) {
            GloryDE50StateAbstract sret = sendGloryOperation(new devices.glory.command.BatchDataTransmition(bills));
            if (sret != null) {
                return sret;
            }
            notifyListeners(COUNTING);
            //? return false?
        }
        // TODO: recheck
        batchEnd = true;
        return this;
    }

    private GloryDE50StateAbstract refreshQuantity() {
        GloryDE50Response response = api.sendGloryOperation(new devices.glory.command.CountingDataRequest());
        if (response.isError()) {
            String error = response.getError();
            Logger.error("Error %s sending cmd : CountingDataRequest", error);
            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
        }
        currentQuantity = response.getBills();
//        for (Integer k : bills.keySet()) {
//            Logger.debug("bill %d %d", k, bills.get(k));
//        }

        return null;
    }

    private boolean isNoCounts() {
        for (Integer i : currentQuantity.values()) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    private void setCurrentQuantity(Map<Integer, Integer> billData) {
        this.currentQuantity = billData;
    }

    private boolean clearQuantity() {
        setCurrentQuantity(new HashMap<Integer, Integer>());
        return true;
    }

    @Override
    public GloryDE50StateAbstract doCancel() {
        return sendGloryOperation(new devices.glory.command.StopCounting());
    }
}
