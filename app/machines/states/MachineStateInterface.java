package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;
import models.db.LgUser;
import models.lov.Currency;

/**
 *
 * @author adji
 */
abstract public interface MachineStateInterface {

    public MachineStatus getStatus();

    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface status);

    public boolean onAcceptDepositEvent();

    public boolean onCancelDepositEvent();

    public boolean onConfirmDepositEvent();

    public boolean onStartBillDeposit(LgUser user, Currency currency, String userCode, Integer userCodeLovId);

    public boolean onStartEnvelopeDeposit(LgUser user, String userCode, Integer userCodeLovId);

    public boolean onStart();

}
