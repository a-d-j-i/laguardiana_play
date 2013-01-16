package devices.glory.manager;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author adji
 */
public interface ManagerInterface {

    static public enum ManagerState {

        NEUTRAL,
        READY_TO_STORE,
        STORING,
        STORED,
        PUT_THE_BILLS_ON_THE_HOPER,
        COUNTING,
        ESCROW_FULL,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,
        CANCELING, COLLECTING,
        JAM,
        ERROR;
    };

    static public enum ManagerError {

        APP_ERROR,
        STORING_ERROR_CALL_ADMIN,
        BILLS_IN_ESCROW_CALL_ADMIN;
    };

    static public class Status extends Observable {

        private ManagerState state = ManagerState.INITIALIZING;
        private ManagerError error;
        private String errorMsg;

        @Override
        synchronized public String toString() {
            return "Error ( " + error + " ) : " + errorMsg;
        }

        synchronized public ManagerState getState() {
            return state;
        }

        synchronized protected void setState(ManagerState state) {
            if (this.state != ManagerState.ERROR) {
                this.state = state;
                setChanged();
                notifyObservers(this);
            }

        }

        synchronized protected void clearError() {
            if (state == ManagerState.ERROR) {
                this.state = ManagerState.INITIALIZING;
                setChanged();
                notifyObservers(this);
            }
        }

        synchronized public String getErrorDetail() {
            return "ErrorDetail{" + "code=" + error + ", data=" + errorMsg + '}';
        }

        synchronized protected void setError(ManagerError e, String msg) {
            this.error = e;
            this.errorMsg = msg;
        }

        synchronized public String name() {
            return state.name();
        }
    }

    public Integer getCurrency();

    public Map<Integer, Integer> getCurrentQuantity();

    public Map<Integer, Integer> getDesiredQuantity();

    public void cancelCommand();

    public boolean storeDeposit(Integer sequenceNumber);

    public boolean withdrawDeposit();

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency);

    public boolean envelopeDeposit();

    public boolean collect();

    public boolean reset();

    public boolean storingErrorReset();

    public Status getStatus();

    public void addObserver(Observer observer);
}
