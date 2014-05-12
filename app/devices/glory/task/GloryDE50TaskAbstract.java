package devices.glory.task;

import devices.device.state.DeviceStateAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 * @param <T>
 */
abstract public class GloryDE50TaskAbstract<T> extends DeviceTaskAbstract<T> {

    abstract protected DeviceStateAbstract call(GloryDE50StateAbstract currentState);

    @Override
    protected DeviceStateInterface call(DeviceStateInterface currentState) {
        return call((GloryDE50StateAbstract) currentState);

    }
}
