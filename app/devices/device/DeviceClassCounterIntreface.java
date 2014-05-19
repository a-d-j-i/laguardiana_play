package devices.device;

import java.util.List;
import java.util.Map;

public interface DeviceClassCounterIntreface {

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency);

    public boolean count(List<Integer> slotList);

    public boolean envelopeDeposit();

    public boolean collect();

    public boolean reset();

    public boolean storingErrorReset();

    public boolean cancelDeposit();

    public boolean storeDeposit(Integer sequenceNumber);

    public boolean withdrawDeposit();

    public Integer getCurrency();

    public Map<Integer, Integer> getCurrentQuantity();

    public Map<Integer, Integer> getDesiredQuantity();

}
