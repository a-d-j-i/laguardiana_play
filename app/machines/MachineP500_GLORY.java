package machines;

import devices.device.DeviceInterface;
import devices.device.DeviceEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import models.db.LgDeviceSlot;

/**
 *
 * @author adji
 */
public class MachineP500_GLORY extends Machine {

    @Override
    protected List<DeviceInterface> getDeviceList() {
        return Arrays.asList();
    }

    public void onDeviceEvent(DeviceEvent deviceEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean count(Integer currency, Map<String, Integer> desiredQuantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
