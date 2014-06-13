package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.status.DeviceStatusInterface;
import devices.glory.GloryDE50Device;
import devices.glory.status.GloryDE50Status;
import devices.mei.MeiEbdsDevice;
import devices.mei.status.MeiEbdsStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import models.db.LgDevice.DeviceType;
import models.db.LgDeviceSlot;
import play.Logger;

/**
 *
 * @author adji
 */
public class MachineP500MEI_GLORY extends Machine {

    MeiEbdsDevice mei = new MeiEbdsDevice("P500_MEI_DEVICE_MEI_EBDS", DeviceType.MEI_EBDS);
    GloryDE50Device glory = new GloryDE50Device("P500_MEI_DEVICE_GLORYDE50", DeviceType.GLORY_DE50);

    @Override
    protected List<DeviceInterface> getDeviceList() {
        return Arrays.asList((DeviceInterface) mei, (DeviceInterface) glory);
    }

    public void onDeviceEvent(DeviceEvent deviceEvent) {
        DeviceInterface source = deviceEvent.getSource();
        DeviceStatusInterface status = deviceEvent.getStatus();
        if (source == mei) {
            switch ((MeiEbdsStatus.MeiEbdsStatusType) status.getType()) {
                case COUNTING:
                    break;
            }
        } else if (source == glory) {
            switch ((GloryDE50Status.GloryDE50StatusType) status.getType()) {
                case COUNTING:
                    break;
            }

        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        Logger.error("Invalid event: %s", deviceEvent);
    }

    public boolean envelopeDeposit() throws InterruptedException, ExecutionException {
        return glory.envelopeDeposit();
    }

    public boolean collect() {
        return glory.collect();
    }

    public boolean cancelDeposit() {
        boolean ret = true;
        if (!mei.cancelDeposit()) {
            Logger.error("Cant cancel mei deposit!!!");
            ret = false;
        }
        if (!glory.cancelDeposit()) {
            Logger.error("Cant cancel glory deposit!!!");
            ret = false;
        }
        return ret;
    }

    public boolean storeDeposit(DeviceInterface device, Integer sequenceNumber) {
        if (device == mei) {
            return mei.storeDeposit(sequenceNumber);
        } else if (device == glory) {
            return glory.storeDeposit(sequenceNumber);
        } else {
            throw new UnsupportedOperationException("invalid device");
        }
    }

    public boolean withdrawDeposit(DeviceInterface device) {
        if (device == mei) {
            return mei.withdrawDeposit();
        } else if (device == glory) {
            return glory.withdrawDeposit();
        } else {
            throw new UnsupportedOperationException("invalid device");
        }
    }

    public boolean count(Integer currency, Map<String, Integer> desiredQuantity) {
        if (mei.count(currency, desiredQuantity)) {
            if (glory.count(currency, desiredQuantity)) {
                return true;
            }
            if (!mei.cancelDeposit()) {
                Logger.error("Cant cancel mei deposit!!!");
            }
        }
        return false;
    }

    @Override
    public boolean isBagInplace() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<LgDeviceSlot, Integer> getCurrentQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<LgDeviceSlot, Integer> getDesiredQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean errorReset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean storingErrorReset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
