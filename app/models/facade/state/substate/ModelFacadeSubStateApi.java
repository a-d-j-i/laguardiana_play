package models.facade.state.substate;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import models.ModelFacade;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class ModelFacadeSubStateApi {

    final protected ModelFacade.ModelFacadeStateApi api;

    public ModelFacadeSubStateApi(ModelFacade.ModelFacadeStateApi api) {
        this.api = api;
    }

    public boolean cancelDeposit(Integer depositId) {
        closeDeposit(depositId, LgDeposit.FinishCause.FINISH_CAUSE_CANCEL);
        return api.getMachine().cancel();
    }

    private void closeDeposit(Integer depositId, LgDeposit.FinishCause finishCause) {
        if (finishCause == LgDeposit.FinishCause.FINISH_CAUSE_OK) {
//                closeBatch();
        }
        Logger.debug("Closing deposit finish cause : %s", finishCause.name());
        LgDeposit d = LgDeposit.findById(depositId);
        d.finishCause = finishCause;
        d.closeDate = new Date();
        d.save();
    }

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
    public boolean cancelDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean cancelWithCause(LgDeposit.FinishCause finishCause) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
