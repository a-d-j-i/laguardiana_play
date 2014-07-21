package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;
import models.db.LgUser;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class MachineStateAbstract implements MachineStateInterface {

    protected final MachineStateApiInterface machine;

    public MachineStateAbstract(MachineStateApiInterface machine) {
        this.machine = machine;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        Logger.debug("%s ignore device %s, status %s", toString(), dev.toString(), st.toString());
    }

    @Override
    public boolean onAcceptDepositEvent() {
        return false;
    }

    @Override
    public boolean onCancelDepositEvent() {
        return false;
    }

    @Override
    public boolean onConfirmDepositEvent() {
        return false;
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatus(null, null, null, null);
    }

    @Override
    public boolean onStart() {
        return true;
    }

    @Override
    public boolean onStartBillDeposit(LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        Logger.error("Can't start bill deposit current state is : %s", this.toString());
        return false;
    }

    @Override
    public boolean onStartEnvelopeDeposit(LgUser user, String userCode, Integer userCodeLovId) {
        Logger.error("Can't start envelope deposit current state is : %s", this.toString());
        return false;
    }
}
