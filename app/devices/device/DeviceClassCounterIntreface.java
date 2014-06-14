package devices.device;

import java.util.Map;
import java.util.concurrent.Future;

public interface DeviceClassCounterIntreface {

    public Future<Boolean> count(Integer currency, Map<String, Integer> desiredQuantity);

    public Future<Boolean> envelopeDeposit();

    public Future<Boolean> collect();

    public Future<Boolean> errorReset();

    public Future<Boolean> storingErrorReset();

    public Future<Boolean> cancelDeposit();

    public Future<Boolean> storeDeposit(Integer sequenceNumber);

    public Future<Boolean> withdrawDeposit();

}
