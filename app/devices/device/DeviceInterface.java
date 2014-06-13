package devices.device;

import devices.device.events.DeviceEventListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import models.db.LgDeviceProperty;

/**
 *
 * @author adji
 */
public interface DeviceInterface {

    public void start();

    public void stop();

    public void addEventListener(DeviceEventListener listener);

    public void removeEventListener(DeviceEventListener listener);

    public Integer getDeviceId();

    public List<LgDeviceProperty> getEditableProperties();

    public LgDeviceProperty setProperty(String property, String value) throws InterruptedException, ExecutionException;

    public boolean clearError();

    public DeviceEvent getLastEvent();

    public Enum getType();

    @Override
    public String toString();

}
