package devices;

import java.util.Map;

public interface DeviceClassCounter {

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency);

    public boolean envelopeDeposit();

    // syncronous cancel and collect.
    public boolean collect();

    public boolean reset();

    public boolean storingErrorReset();

    public Integer getCurrency();

    public Map<Integer, Integer> getCurrentQuantity();

    public Map<Integer, Integer> getDesiredQuantity();

    public void cancelCommand();

    public boolean storeDeposit(Integer sequenceNumber);

    public boolean withdrawDeposit();

}
