package devices.glory.state.poll;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;
import devices.glory.response.GloryDE50ResponseWithData;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.deposit;
import static devices.glory.response.GloryDE50ResponseWithData.D1Mode.neutral;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.abnormal_device;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_recover_from_storing_error;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_restoration;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.being_store;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.counting;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.counting_start_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_close_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.escrow_open;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_error;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.storing_start_request;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.waiting;
import static devices.glory.response.GloryDE50ResponseWithData.SR1Mode.waiting_for_an_envelope_to_set;
import devices.glory.state.GloryDE50StateError;
import devices.glory.state.GloryDE50StateError.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.GloryDE50StateWaitForOperation;
import devices.glory.state.GloryDE50StateWaitForResponse.GloryDE50StateWaitForResponseCallback;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.CANCELING;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.COUNTING;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.ESCROW_FULL;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.JAM;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.PUT_THE_BILLS_ON_THE_HOPER;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.READY_TO_STORE;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_THE_BILLS_FROM_ESCROW;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.REMOVE_THE_BILLS_FROM_HOPER;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.STORING;
import devices.glory.status.GloryDE50StatusCurrentCount;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateCount extends GloryDE50StatePoll {

    private boolean needToStoreDeposit = false;
    private boolean needToWithdrawDeposit = false;

    private Map<String, Integer> currentQuantity = new HashMap<String, Integer>();
    final private Map<String, Integer> desiredQuantity;
    final private Integer currency;

    private final int MAX_COUNT_RETRIES = 10;
    private int currentSlot;
    private final boolean isBatch;
    boolean fakeCount = false;
    int count_retries = 0;
    boolean batchEnd = false;

    public GloryDE50StateCount(GloryDE50Device api, Map<String, Integer> desiredQuantity, Integer currency) {
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
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskStore) {
            needToStoreDeposit = true;
            task.setReturnValue(true);
            return sense();
        } else if (task instanceof DeviceTaskWithdraw) {
            needToWithdrawDeposit = true;
            task.setReturnValue(true);
            return sense();
        } else if (task instanceof DeviceTaskCancel) {
            Logger.debug("doCancel");
            task.setReturnValue(true);
            api.notifyListeners(CANCELING);
            return sendGloryOperation(new devices.glory.operation.StopCounting(), new GloryDE50StateWaitForResponseCallback() {

                public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                    return new GloryDE50StateGotoNeutral(api, new GloryDE50StateWaitForOperation(api), true, true);
                }
            });
        } else {
            return super.call(task);
        }
    }

    @Override
    public GloryDE50StateAbstract poll(final GloryDE50ResponseWithData lastResponse) {
        Logger.debug("CountCommand Start CURRENCY %d sense : %s", currency.byteValue(), lastResponse.toString());
        if (lastResponse.isCassetteFullCounter()) {
            return new GloryDE50StateRotateCassete(api, this);
        }
        // If I'm not in deposit mode try to get there once
        switch (lastResponse.getD1Mode()) {
            case deposit:
                break;
            case neutral:
                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                    return this;
                } else {
                    Logger.error("CountCommand CURRENCY %d", currency.byteValue());
                    return sendGloryOperation(new devices.glory.operation.SwitchCurrency(currency.byteValue()), new GloryDE50StateWaitForResponseCallback() {

                        public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                            return sendGloryOperation(new devices.glory.operation.SetDepositMode());
                        }
                    });
                }
        }

        switch (lastResponse.getSr1Mode()) {
            case storing_start_request:
                // When there is a fake count but there are bills I get directly a storing_start_request
                // Send the missing event here.
                if (fakeCount) {
                    fakeCount = false;
                    api.notifyListeners(COUNTING);
                    return refreshQuantity(null);
                }
                if (lastResponse.isRejectBillPresent()) {
                    api.notifyListeners(REMOVE_REJECTED_BILLS);
                    break;
                }
                if (needToStoreDeposit) {
                    // We clear the counter because they are invalid now
                    clearQuantity();
                    return sendGloryOperation(new devices.glory.operation.StoringStart(0), new GloryDE50StateWaitForResponseCallback() {

                        public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                            api.notifyListeners(STORING);
                            return GloryDE50StateCount.this;
                        }
                    });
                } else if (needToWithdrawDeposit) {
                    api.setClosing(false);
                    return sendGloryOperation(new devices.glory.operation.OpenEscrow());
                    // countData.withdrawDepositDone();
                } else {
                    if (isBatch && batchEnd) {
                        return this;
                    }
                    // We need a valid counters before generating the events.
                    return refreshQuantity(new GloryDE50StateWaitForResponseCallback() {

                        public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                            if (lastResponse.isEscrowFull()) {
                                api.notifyListeners(ESCROW_FULL);
                            } else if (lastResponse.isHopperBillPresent()) {
                                return batchCountStart();
                            } else {
                                api.notifyListeners(READY_TO_STORE);
                            }
                            return GloryDE50StateCount.this;
                        }
                    });
                }
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
                return sendGloryOperation(new devices.glory.operation.CloseEscrow(), new GloryDE50StateWaitForResponseCallback() {

                    public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                        api.setClosing(true);
                        return GloryDE50StateCount.this;
                    }
                });
            case counting:
                if (!fakeCount) {
                    api.notifyListeners(COUNTING);
                    // The second time after storing.
                    // Ignore error.
                    return refreshQuantity(null);
                }
                break;
            case waiting:
                return refreshQuantity(new GloryDE50StateWaitForResponseCallback() {

                    public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                        if (!lastResponse.isHopperBillPresent()) {
                            api.notifyListeners(PUT_THE_BILLS_ON_THE_HOPER);
                        }
                        count_retries = 1;
                        return GloryDE50StateCount.this;
                    }
                });
            case being_store:
                fakeCount = true;
                needToStoreDeposit = false;
                break;
            case counting_start_request:
                needToStoreDeposit = false;
                return refreshQuantity(new GloryDE50StateWaitForResponseCallback() {

                    public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                        if (lastResponse.isRejectBillPresent()) {
                            api.notifyListeners(REMOVE_REJECTED_BILLS);
                            return GloryDE50StateCount.this;
                        }
                        if (isNoCounts()) {
                            if (count_retries > MAX_COUNT_RETRIES) {
                                Logger.error("Error in hopper sensor");
                                api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                                //setGloryDE50Error(new GloryManagerGloryDE50Error(GloryManagerError.ERROR_CODE.GLORY_MANAGER_ERROR, "Error in hopper sensor"));
                                return GloryDE50StateCount.this;
                            } else {
                                count_retries++;
                            }
                        } else {
                            count_retries = 1;
                        }

                        fakeCount = false;
                        needToWithdrawDeposit = false;
                        if (!needToStoreDeposit) {
                            // If there are bills in the hoper then it comes here after storing a full escrow
                            if (isBatch && batchEnd) { //BATCH END
                                api.setClosing(false);
                                return sendGloryOperation(new devices.glory.operation.OpenEscrow(), new GloryDE50StateWaitForResponseCallback() {

                                    public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                                        return new GloryDE50StateGotoNeutral(api, GloryDE50StateCount.this, true, true);
                                    }
                                });
                            }
                            if (lastResponse.isRejectBillPresent()) {
                                api.notifyListeners(REMOVE_REJECTED_BILLS);
                                return GloryDE50StateCount.this;
                            }
                            return batchCountStart();
                        }
                        return GloryDE50StateCount.this;
                    }
                });
            case abnormal_device:
                api.notifyListeners(JAM);
                return new GloryDE50StateGotoNeutral(api, this, true, true);
            case storing_error:
                return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.STORING_ERROR_CALL_ADMIN, String.format("Count Storing error, todo: get the flags"));
            default:
                return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, String.format("Count invalid sr1 mode %s", lastResponse.getSr1Mode().name()));
        }
        return this;
    }

    GloryDE50StateAbstract batchCountStart() {
        final int[] bills = new int[32];

        // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
        for (int i = 0; i < bills.length; i++) {
            bills[ i] = 0;
        }
        if (!isBatch) {
            // Sometimes the BatchDataTransmition fails, trying randomly to see what can be.
            return sendGloryOperation(new devices.glory.operation.BatchDataTransmition(bills), new GloryDE50StateWaitForResponseCallback() {

                public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                    api.notifyListeners(COUNTING);
                    return GloryDE50StateCount.this;
                }

                @Override
                public DeviceStateInterface onError(GloryDE50Device api, GloryDE50OperationInterface operation, GloryDE50ResponseError response) {
                    api.notifyListeners(REMOVE_THE_BILLS_FROM_HOPER);
                    return GloryDE50StateCount.this;
                }

            });
        }
        Logger.debug("ISBATCH");
        return sendGloryOperation(new devices.glory.operation.CountingDataRequest(), new GloryDE50StateWaitForResponseCallback() {

            public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                GloryDE50ResponseWithData data = (GloryDE50ResponseWithData) response;
                Map<String, Integer> currentQ = data.getBills();
                if (currentQ == null) {
                    String err = String.format("Error getting current count");
                    Logger.error("Error %s sending cmd : CountingDataRequest", err);
                    return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
                }

                while (currentSlot < 32) {
                    int desired = 0;
                    if (desiredQuantity.get(Integer.toString(currentSlot)) != null) {
                        desired = desiredQuantity.get(Integer.toString(currentSlot));
                    }
                    int current = currentQ.get(Integer.toString(currentSlot));
                    if (current > desired) {
                        String err = String.format("Invalid bill value %d %d %d", currentSlot, current, desired);
                        Logger.error("Error %s sending cmd : CountingDataRequest", err);
                        return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
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
                return GloryDE50StateCount.this;
            }
        });
    }

    private GloryDE50StateAbstract refreshQuantity(final GloryDE50StateWaitForResponseCallback callback) {
        return sendGloryOperation(new devices.glory.operation.CountingDataRequest(), new GloryDE50StateWaitForResponseCallback() {

            public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
//                GloryDE50ResponseWithData data = (GloryDE50ResponseWithData) response;
//                Map<Integer, Integer> bs = data.getBills();
//                currentQuantity.set(bs);
//                for (Integer k : bs.keySet()) {
//                    Logger.debug("bill %d %d", k, bs.get(k));
//                }
                GloryDE50ResponseWithData data = (GloryDE50ResponseWithData) response;
                Map<String, Integer> currentQ = data.getBills();
                if (currentQ == null) {
                    String err = String.format("Error getting current count");
                    Logger.error("Error %s sending cmd : CountingDataRequest", err);
                    return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
                }
                currentQuantity = currentQ;
                api.notifyListeners(new GloryDE50StatusCurrentCount(currentQuantity, desiredQuantity, currency));
                if (callback != null) {
                    return callback.onResponse(operation, response);
                }
                return GloryDE50StateCount.this;
            }
        }
        );
    }

    private boolean isNoCounts() {
        for (Integer i : currentQuantity.values()) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean clearQuantity() {
        currentQuantity = new HashMap<String, Integer>();
        return true;
    }

    @Override
    public String toString() {
        return "GloryDE50StateCount{" + "currentQuantity=" + currentQuantity + ", desiredQuantity=" + desiredQuantity + ", currency=" + currency + ", MAX_COUNT_RETRIES=" + MAX_COUNT_RETRIES + ", currentSlot=" + currentSlot + ", isBatch=" + isBatch + ", fakeCount=" + fakeCount + ", count_retries=" + count_retries + ", batchEnd=" + batchEnd + '}';
    }

}
