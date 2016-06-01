package machines.status;

/**
 *
 * @author adji
 */
public class MachineStatus {

    private final Integer currentUserId;
    private final String neededAction;
    private final String stateName;

    public MachineStatus(Integer currentUserId, String neededAction, String stateName) {
        this.currentUserId = currentUserId;
        this.neededAction = neededAction;
        this.stateName = stateName;
    }

    public String getNeededAction() {
        return neededAction;
    }

    public String getStateName() {
        return stateName;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public String toString() {
        return "MachineStatus{" + "currentUser=" + currentUserId + ", neededAction=" + neededAction + ", stateName=" + stateName + '}';
    }
}
