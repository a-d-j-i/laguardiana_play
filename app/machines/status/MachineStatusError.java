package machines.status;

/**
 *
 * @author adji
 */
public class MachineStatusError extends MachineStatus {

    private final String error;

    public MachineStatusError(Integer currentUserId, String neededController, String neededAction, String stateName, String message, String error) {
        super(currentUserId, neededAction, stateName, message);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "MachineStatusError{" + "error=" + error + '}';
    }

}
