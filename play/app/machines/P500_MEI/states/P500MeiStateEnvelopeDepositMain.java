package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import static devices.ioboard.response.IoboardStateResponse.BAG_STATE.BAG_STATE_INPLACE;
import devices.ioboard.status.IoBoardStatusError;
import devices.ioboard.status.IoboardStatus;
import devices.ioboard.status.IoboardStatus.IoboardBagApprovedState;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.status.MachineEnvelopeDepositStatus;
import machines.status.MachineStatus;
import models.Configuration;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateEnvelopeDepositMain extends MachineStateAbstract {

    protected final P500MEIStateContext context;

    public P500MeiStateEnvelopeDepositMain(P500MEIStateContext context) {
        this.context = context;
    }

    @Override
    public boolean onStart() {
        Logger.debug("DEPOSIT ID : %d", context.getDepositId());
        EnvelopeDeposit envelopeDeposit = EnvelopeDeposit.findById(context.getDepositId());
        if (envelopeDeposit != null) {
            Logger.info("Trying to print deposit ticket: %d", envelopeDeposit.depositId);
            envelopeDeposit.printStart();
        } else {
            Logger.info("Deposit: %d not found", envelopeDeposit.depositId);
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(IoboardStatus.class)) {
            IoboardStatus iobs = (IoboardStatus) st;
            if (iobs.getBagState() != BAG_STATE_INPLACE && !Configuration.isIgnoreBag()) {
                context.setCurrentState(new MachineStateAbstract() {

                    @Override
                    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                        Logger.debug("BAG REMOVED DEVICE EVENT %s, %s", dev.toString(), st.toString());
                        if (st.is(IoboardStatus.class)) {
                            IoboardStatus s = (IoboardStatus) st;
                            if (s.getBagApprovedState() == IoboardBagApprovedState.BAG_APROVED) {
                                context.setCurrentState(P500MeiStateEnvelopeDepositMain.this);
                            }
                        } else {
                            P500MeiStateEnvelopeDepositMain.this.onDeviceEvent(dev, st);
                        }
                    }

                    @Override
                    public MachineStatus getStatus() {
                        return P500MeiStateEnvelopeDepositMain.this.getStatus("BAG_REMOVED");
                    }

                    @Override
                    public boolean onCancelDepositEvent() {
                        return P500MeiStateEnvelopeDepositMain.this.onCancelDepositEvent();
                    }

                }
                );
            }
            return;
        } else if (st.is(DeviceStatusError.class)) {
            if (st.is(IoBoardStatusError.class) && ((IoBoardStatusError) st).canIgnore()) {
                return;
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
    public boolean onAcceptDepositEvent() {
        return context.setCurrentState(new P500MeiStateEnvelopeDepositFinish(context, LgDeposit.FinishCause.FINISH_CAUSE_OK));
    }

    @Override
    public boolean onCancelDepositEvent() {
        return context.setCurrentState(new P500MeiStateEnvelopeDepositFinish(context, LgDeposit.FinishCause.FINISH_CAUSE_CANCEL));
    }

    @Override
    public MachineStatus getStatus() {
        return getStatus("READY_TO_STORE_SENSORLESS");
    }

    public MachineStatus getStatus(String stateName) {
        if (context.getDepositId() == null) {
            MachineStateAbstract errorSt = new P500MeiStateError(new P500MeiStateWaiting(context), context, "Deposit id is null");
            context.setCurrentState(errorSt);
            return errorSt.getStatus();
        }
        return new MachineEnvelopeDepositStatus(context.getDepositId(), context.getCurrentUserId(), "EnvelopeDepositController.mainloop", stateName);
    }

    @Override
    public String toString() {
        return "P500MeiStateEnvelopeDepositMain{" + "context=" + context.toString() + '}';
    }
}
