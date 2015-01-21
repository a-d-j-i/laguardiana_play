package devices.device.status;

import java.util.Map;

public interface DeviceStatusCountIntreface extends DeviceStatusInterface {

    public Map<String, Integer> getCurrentQuantity();

    public Map<String, Integer> getDesiredQuantity();

}
