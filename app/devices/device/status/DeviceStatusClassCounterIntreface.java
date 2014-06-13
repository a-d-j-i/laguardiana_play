package devices.device.status;

import java.util.Map;

public interface DeviceStatusClassCounterIntreface extends DeviceStatusInterface {

    public Integer getCurrency();

    public Map<Integer, Integer> getCurrentQuantity();

    public Map<Integer, Integer> getDesiredQuantity();

}
