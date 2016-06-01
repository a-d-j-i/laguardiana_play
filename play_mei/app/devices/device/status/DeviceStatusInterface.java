package devices.device.status;

/**
 *
 * @author adji
 */
public interface DeviceStatusInterface {

    public boolean is(Enum type);

    public boolean is(Class type);

    public boolean dontLog();
}
