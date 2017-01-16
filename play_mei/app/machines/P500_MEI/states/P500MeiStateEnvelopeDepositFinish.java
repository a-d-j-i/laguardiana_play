package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import devices.ioboard.status.IoBoardStatusError;
import devices.ioboard.status.IoboardStatus;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.status.MachineEnvelopeDepositStatus;
import models.BillDeposit;
import models.Configuration;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateEnvelopeDepositFinish extends MachineStateAbstract {

    private final BillDeposit.FinishCause finishCause;
    private final P500MEIStateContext context;

    public P500MeiStateEnvelopeDepositFinish(P500MEIStateContext context, BillDeposit.FinishCause finishCause) {
        this.context = context;
        this.finishCause = finishCause;
    }

    @Override
    public boolean onStart() {
        EnvelopeDeposit envelopeDeposit = EnvelopeDeposit.findById(context.getDepositId());
        if (envelopeDeposit != null) {
            Logger.info("Trying to print deposit: %d", envelopeDeposit.depositId);
            envelopeDeposit.closeDeposit(finishCause);
            if (finishCause != LgDeposit.FinishCause.FINISH_CAUSE_CANCEL) {
                Logger.info("Printing deposit: %d", envelopeDeposit.depositId);
                envelopeDeposit.print(false);
            }
        } else {
            Logger.info("Deposit: %d not found", envelopeDeposit.depositId);
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(DeviceStatusError.class)) {
            if (st.is(IoBoardStatusError.class)) {
                if (Configuration.isIgnoreIoBoard()) {
                    return;
                }
            }
            DeviceStatusError err = (DeviceStatusError) st;
            Logger.error("DEVICE ERROR : %s", err.getError());
            context.setCurrentState(new P500MeiStateError(this, context, err.getError()));
            return;
        } else if (st.is(MeiEbdsStatus.NEUTRAL)) {
            // ignore is ok.
            return;
        } else if (st.is(IoboardStatus.class)) {
            // ignore is ok.
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineEnvelopeDepositStatus getStatus() {
        return new MachineEnvelopeDepositStatus(context.getDepositId(), context.getCurrentUserId(), "EnvelopeDepositController.finish", "FINISH");
    }

    @Override
    public boolean onConfirmDepositEvent() {
        return context.setCurrentState(new P500MeiStateWaiting(context));
    }

    @Override
    public String toString() {
        return "P500MeiStateEnvelopeDepositFinish{" + "finishCause=" + finishCause + ", context=" + context + '}';
    }

}
