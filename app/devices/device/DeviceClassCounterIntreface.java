package devices.device;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface DeviceClassCounterIntreface {

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) throws InterruptedException, ExecutionException;

    public boolean count(List<Integer> slotList) throws InterruptedException, ExecutionException;

    public boolean envelopeDeposit() throws InterruptedException, ExecutionException;

    public boolean collect();

    public boolean reset();

    public boolean storingErrorReset();

    public boolean cancelDeposit();

    public boolean storeDeposit(Integer sequenceNumber) throws InterruptedException, ExecutionException;

    public boolean withdrawDeposit();

}
