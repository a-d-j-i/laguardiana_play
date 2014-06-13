package machines.events;

import java.util.EventListener;

/**
 *
 * @author adji
 */
public interface MachineEventListener extends EventListener {

    void onMachineEvent(MachineEvent evt);
}
