package models.facade.state;

import machines.events.MachineEvent;
import models.ModelFacade;
import models.facade.state.substate.ModelFacadeSubStateAbstract;
import models.facade.status.ModelFacadeStateStatus;

/**
 *
 * @author adji
 */
abstract public class ModelFacadeStateAbstract {

    protected final ModelFacade.ModelFacadeStateApi api;

    public ModelFacadeStateAbstract(ModelFacade.ModelFacadeStateApi api) {
        this.api = api;
    }

    protected ModelFacadeSubStateAbstract subState = null;

//    public class StateApi {
//
//        final private TimeoutTimer timer;
//
//        public StateApi() {
//            timer = new TimeoutTimer(ModelFacadeStateAbstract.this);
//        }
//
//        public void setState(ActionState state) {
//            ModelFacadeStateAbstract.this.state = state;
//        }
//
//        public void count() {
//            userActionApi.count(currency.numericId);
//        }
//
//        public void cancelDeposit() {
//            userActionApi.cancelDeposit();
//        }
//
//        public boolean store() {
//            return userActionApi.store(currentDepositId);
//        }
//
//        public boolean isIoBoardOk() {
//            return userActionApi.isIoBoardOk();
//        }
//
//        public void withdraw() {
//            userActionApi.withdraw();
//        }
//
//        public List<LgBill> getCurrentBillList() {
//            return userActionApi.getCurrentBillList();
//        }
//
//        public ItemQuantity getCurrentItemQuantity() {
//            LgBag currentBag = LgBag.getCurrentBag();
//            return currentBag.getItemQuantity(currentDepositId);
//        }
//
//        public void addBatchToDeposit(LgDevice device) {
//            LgDeposit deposit = LgDeposit.findById(currentDepositId);
//            LgBatch batch = new LgBatch(device);
//            for (LgBill bill : userActionApi.getCurrentBillList()) {
//                Logger.debug(" -> quantity %d", bill.quantity);
//                batch.addBill(bill);
//            }
//            deposit.addBatch(batch);
//            batch.save();
//            deposit.save();
//            currentBatchId = batch.batchId;
//        }
//
//        public void closeBatch() {
//            if (currentBatchId != null) {
//                LgBatch b = LgBatch.findById(currentBatchId);
//                if (b == null) {
//                    Logger.error("current batch is null, batch id %d", currentBatchId);
//                } else {
//                    if (b.finishDate == null) {
//                        b.finishDate = new Date();
//                        b.save();
//                    }
//                }
//            }
//        }
//
//        public void closeDeposit(FinishCause finishCause) {
//            if (finishCause == FinishCause.FINISH_CAUSE_OK) {
//                closeBatch();
//            }
//            Logger.debug("Closing deposit finish cause : %s", finishCause.name());
//            LgDeposit d = LgDeposit.findById(currentDepositId);
//            d.finishCause = finishCause;
//            d.closeDate = new Date();
//            d.save();
//        }
//
//        public void startTimer() {
//            timer.start();
//        }
//
//        public void restartTimer() {
//            timer.restart();
//        }
//
//        public void cancelTimer() {
//            timer.cancel();
//        }
//
//        public void setError(ModelError.ERROR_CODE errorCode, String detail) {
//            userActionApi.setError(errorCode, detail);
//        }
//
//        public void openGate() {
//            userActionApi.openGate();
//        }
//
//        public void closeGate() {
//            userActionApi.closeGate();
//        }
//
////        public ManagerInterface.MANAGER_STATE getManagerState() {
////            return userActionApi.getManagerState();
////        }
//    }
//    public boolean start() {
//        if (!isIoBoardOk(ioBoard.getStatus())) {
//            modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "Bag not in place");
//            return;
//        }
    // Close any old unfinished deposit.
//        LgDeposit.closeUnfinished();
//    }
    public boolean accept() {
        return subState.accept();
    }

    public boolean cancel() {
        return subState.cancel();
    }

    public boolean suspendTimeout() {
        return false;
    }

    // Delegate
    public void onMachineEvent(MachineEvent evt) {
        subState.onMachineEvent(evt);
    }

    // The only state that implements this is StateWaiting.

    public boolean startAction(ModelFacadeStateAbstract userAction) {
        return false;
    }

    abstract public ModelFacadeStateAbstract init();

    abstract public boolean finish();

    abstract public ModelFacadeStateStatus getStatus();

}
