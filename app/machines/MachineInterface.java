package machines;

import java.util.List;
import java.util.concurrent.Future;
import machines.jobs.MachineJob;

/**
 *
 * @author adji
 */
public interface MachineInterface {

    public void start();

    public void stop();

    public MachineDeviceDecorator findDeviceById(Integer deviceId);

    public List<MachineDeviceDecorator> getDevices();

    public Future submit(MachineJob job);

    public <V> V execute(MachineJob<V> job);

}
