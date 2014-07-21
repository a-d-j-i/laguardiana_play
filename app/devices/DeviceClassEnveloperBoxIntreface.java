package devices;

import java.util.concurrent.Future;

public interface DeviceClassEnveloperBoxIntreface {

    public Future<Boolean> envelopeDeposit();

    public Future<Boolean> cancelDeposit();

}
