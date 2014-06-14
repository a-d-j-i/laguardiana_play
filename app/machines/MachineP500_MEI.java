package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import devices.device.status.DeviceStatusInterface;
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
public class MachineP500_MEI extends Machine {

    MeiEbdsDevice mei = new MeiEbdsDevice("P500_MEI_DEVICE_MEI_EBDS", DeviceType.MEI_EBDS);

    @Override
    protected List<DeviceInterface> getDeviceList() {
        return Arrays.asList((DeviceInterface) mei);
    }

    public void onDeviceEvent(DeviceEvent deviceEvent) {
        DeviceInterface source = deviceEvent.getSource();
        DeviceStatusInterface status = deviceEvent.getStatus();
        if (source == mei) {
            switch ((MeiEbdsStatus.MeiEbdsStatusType) status.getType()) {
                case COUNTING:
                    break;
                case READY_TO_STORE:
                    mei.storeDeposit(1);
                    break;
            }
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        Logger.error("Invalid event: %s", deviceEvent);
    }

    public boolean envelopeDeposit() {
        return false;
    }

    public boolean collect() {
        return false;
    }

    public boolean cancelDeposit() {
        try {
            if (mei.cancelDeposit().get()) {
                return true;
            }
        } catch (InterruptedException ex) {
            Logger.debug("Exception on Machine cancelDeposit ", ex);
        } catch (ExecutionException ex) {
            Logger.debug("Exception on Machine cancelDeposit ", ex);
        }
        Logger.error("Cant cancel mei deposit!!!");
        return false;
    }

    public boolean storeDeposit(DeviceInterface device, Integer sequenceNumber) {
        if (device == mei) {
            try {
                return mei.storeDeposit(sequenceNumber).get();
            } catch (InterruptedException ex) {
                Logger.debug("Exception on Machine cancelDeposit ", ex);
            } catch (ExecutionException ex) {
                Logger.debug("Exception on Machine cancelDeposit ", ex);
            }
            return false;
        } else {
            throw new UnsupportedOperationException("invalid device");
        }
    }

    public boolean withdrawDeposit(DeviceInterface device) {
        if (device == mei) {
            try {
                return mei.withdrawDeposit().get();
            } catch (InterruptedException ex) {
                Logger.debug("Exception on Machine cancelDeposit ", ex);
            } catch (ExecutionException ex) {
                Logger.debug("Exception on Machine cancelDeposit ", ex);
            }
            return false;

        } else {
            throw new UnsupportedOperationException("invalid device");
        }
    }

    public boolean count(Integer currency, Map<String, Integer> desiredQuantity) {
        try {
            if (mei.count(currency, desiredQuantity).get()) {
                return true;
            }
            if (!mei.cancelDeposit().get()) {
                Logger.error("Cant cancel mei deposit!!!");
            }
        } catch (InterruptedException ex) {
            Logger.debug("Exception on Machine cancelDeposit ", ex);
        } catch (ExecutionException ex) {
            Logger.debug("Exception on Machine cancelDeposit ", ex);
        }
        return false;

    }

    @Override
    public boolean isBagInplace() {
        return false;
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
