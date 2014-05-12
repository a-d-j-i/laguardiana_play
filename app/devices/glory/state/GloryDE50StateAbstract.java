package devices.glory.state;

import devices.device.DeviceStatus;
import devices.device.state.DeviceStateInterface;
import devices.device.state.DeviceStateOperation;
import devices.glory.GloryDE50Device.GloryDE50StateApi;
import java.util.Map;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract extends DeviceStateOperation implements DeviceStateInterface {

    public GloryDE50StateAbstract(GloryDE50StateApi api) {
        super(api);
    }

    @Override
    public GloryDE50StateApi getApi() {
        return (GloryDE50StateApi) super.getApi();
    }

    public boolean acceptCollect() {
        return false;
    }

    public boolean acceptCount() {
        return false;
    }

    public boolean acceptEnvelopeDeposit() {
        return false;
    }

    public boolean acceptReset() {
        return false;
    }

    public boolean acceptStoringReset() {
        return false;
    }

    public boolean acceptOpenPort() {
        return false;
    }

    public boolean acceptStore() {
        return false;
    }

    public boolean acceptWithdraw() {
        return false;
    }

    public boolean cancelDeposit() {
        return false;
    }

    public Integer getCurrency() {
        return null;
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return null;
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return null;
    }

    protected void notifyListeners(DeviceStatus.STATUS status) {
        getApi().notifyListeners(status);
    }

    public boolean clearError() {
        return false;
    }

    public String getError() {
        return null;
    }

}
