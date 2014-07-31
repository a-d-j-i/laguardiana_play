package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.EnvelopeDeposit;

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

    public boolean onStartBillDeposit(BillDeposit refBillDeposit);

    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refEnvelopeDeposit);

    public boolean onStart();

}