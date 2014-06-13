package machines.status;

/**
 *
 * @author adji
 */
public class MachineStatusError extends MachineStatus {

    private final String error;

    public MachineStatusError(String error) {
        super(MachineStatusType.ERROR);
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
