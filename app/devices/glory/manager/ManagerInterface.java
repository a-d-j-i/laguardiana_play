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
        CANCELING, BAG_COLLECTED,
        JAM,
        ERROR;
    };

    static public class State extends Observable {

        private ManagerState state = ManagerState.INITIALIZING;
        private GloryManagerError error;

        private State(State aThis) {
            this.state = aThis.state;
            this.error = aThis.error;
        }

        public State() {
        }

        @Override
        synchronized public String toString() {
            return "Error ( " + error + " ) ";
        }

        synchronized public ManagerState getState() {
            return state;
        }

        synchronized protected void setState(ManagerState state) {
            if (this.state != ManagerState.ERROR) {
                this.state = state;
                setChanged();
                notifyObservers(new State(this));
            }
        }

        synchronized protected void clearError() {
            if (state == ManagerState.ERROR) {
                this.state = ManagerState.INITIALIZING;
                setChanged();
                notifyObservers(new State(this));
            }
        }

        synchronized public GloryManagerError getError() {
            return error;
        }

        synchronized protected void setError(GloryManagerError e) {
            this.error = e;
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

    public State getStatus();

    public void addObserver(Observer observer);
}
