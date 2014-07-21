package machines.status;

import play.i18n.Messages;

/**
 *
 * @author adji
 */
public class MachineStatus {

    private final Integer currentUserId;
    private final String neededAction;
    private final String stateName;
    private final String message;

    public MachineStatus(Integer currentUserId, String neededAction, String stateName, String message) {
        this.currentUserId = currentUserId;
        this.neededAction = neededAction;
        this.stateName = stateName;
        this.message = message;
    }

    public String getNeededAction() {
        return neededAction;
    }

    public String getStateName() {
        return stateName;
    }

    public String getMessage() {
        return Messages.get(message);
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public String toString() {
        return "MachineStatus{" + "currentUser=" + currentUserId + ", neededAction=" + neededAction + ", stateName=" + stateName + ", message=" + message + '}';
    }
}
