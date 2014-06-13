package devices.device;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface DeviceClassCounterIntreface {

    public boolean count(Integer currency, Map<String, Integer> desiredQuantity);

    public boolean envelopeDeposit();

    public boolean collect();

    public boolean errorReset();

    public boolean storingErrorReset();

    public boolean cancelDeposit();

    public boolean storeDeposit(Integer sequenceNumber);

    public boolean withdrawDeposit();

}
