package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import models.BillDeposit;
import models.EnvelopeDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class MachineStateAbstract implements MachineStateInterface {

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
//        Logger.debug("%s ignore device %s, status %s", toString(), dev.toString(), st.toString());
    }

    @Override
    public boolean onAcceptDepositEvent() {
        Logger.error("Can't accept deposit");
        return false;
    }

    @Override
    public boolean onCancelDepositEvent() {
        Logger.error("Can't cancel deposit");
        return false;
    }

    @Override
    public boolean onConfirmDepositEvent() {
        Logger.error("Can't confirm deposit");
        return false;
    }

    @Override
    public boolean onStart() {
        return true;
    }

    @Override
    public boolean onStartBillDeposit(BillDeposit refBillDeposit) {
        Logger.error("Can't start bill deposit current state is : %s", this.toString());
        return false;
    }

    @Override
    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refEnvelopeDeposit) {
        Logger.error("Can't start envelope deposit current state is : %s", this.toString());
        return false;
    }

    public boolean onReset() {
        Logger.error("Can't reset device %s", this.toString());
        return false;
    }

    public boolean onStoringErrorReset() {
        Logger.error("Can't storing reset device %s", this.toString());
        return false;
    }

}
