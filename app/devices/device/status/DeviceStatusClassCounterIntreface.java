package devices.device.status;

import java.util.Map;

public interface DeviceStatusClassCounterIntreface extends DeviceStatusInterface {

    public Integer getCurrency();

    public Map<String, Integer> getCurrentQuantity();

    public Map<String, Integer> getDesiredQuantity();

}
