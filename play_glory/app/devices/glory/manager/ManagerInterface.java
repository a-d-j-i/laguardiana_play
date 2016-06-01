package devices.glory.manager;

import devices.glory.Glory;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author adji
 */
public interface ManagerInterface {

    static public enum MANAGER_STATE {

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

    public class ManagerStatus {

        final private MANAGER_STATE state;
        final private GloryManagerError error;
        final private Map<Integer, Integer> bills;

        protected ManagerStatus(State aThis) {
            this.state = aThis.state;
            this.error = aThis.error;
            bills = new HashMap<Integer, Integer>();
        }

        private ManagerStatus(Map<Integer, Integer> bills) {
            this.state = MANAGER_STATE.STORING;
            this.error = null;
            this.bills = bills;
        }

        @Override
        public String toString() {
            return "ManagerStatus{" + "state=" + state + ", error=" + error + ", bills=" + bills + '}';
        }

        public Map<Integer, Integer> getBills() {
            return bills;
        }

        public MANAGER_STATE getState() {
            return state;
        }

        public GloryManagerError getError() {
            return error;
        }

        public String name() {
            return state.name();
        }
    }

    class State extends Observable {

        private MANAGER_STATE state = MANAGER_STATE.INITIALIZING;
        private GloryManagerError error;
        private boolean closing = false;

        @Override
        synchronized public String toString() {
            return "Error ( " + error + " ) ";
        }

        synchronized ManagerStatus getStatus() {
            return new ManagerStatus(this);
        }

        synchronized public void setState(Map<Integer, Integer> bills) {
            if (this.state != MANAGER_STATE.ERROR) {
                setChanged();
                notifyObservers(new ManagerStatus(bills));
            }
        }

        synchronized void setState(MANAGER_STATE state) {
            if (this.state != MANAGER_STATE.ERROR) {
                if (this.state != state) {
                    this.state = state;
                    setChanged();
                    notifyObservers(new ManagerStatus(this));
                }
            }
        }

        synchronized void clearError() {
            if (state == MANAGER_STATE.ERROR) {
                this.state = MANAGER_STATE.INITIALIZING;
                this.error = null;
                setChanged();
                notifyObservers(new ManagerStatus(this));
            }
        }

        synchronized GloryManagerError getError() {
            return error;
        }

        synchronized void setError(GloryManagerError e) {
            this.error = e;
            setChanged();
            notifyObservers(new ManagerStatus(this));
        }

        synchronized public boolean isClosing() {
            return closing;
        }

        synchronized public void setClosing(boolean closing) {
            this.closing = closing;
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

    public ManagerStatus getStatus();

    public void addObserver(Observer observer);

    public Glory getCounter();
}
