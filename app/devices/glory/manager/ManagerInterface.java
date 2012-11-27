/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author adji
 */
public interface ManagerInterface {

    static public enum State {

        IDLE,
        READY_TO_STORE,
        STORING,
        ERROR,
        PUT_THE_BILLS_ON_THE_HOPER,
        COUNTING,
        ESCROW_FULL,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,
        CANCELING, CANCELED, COLLECTING;
    };

    static public enum Error {

        APP_ERROR,
        JAM,
        STORING_ERROR_CALL_ADMIN,
        BILLS_IN_ESCROW_CALL_ADMIN;
    };

    static public class Status extends Observable {

        private State state = State.IDLE;
        private Error error;
        private String errorMsg;

        @Override
        synchronized public String toString() {
            return "Error ( " + error + " ) : " + errorMsg;
        }

        synchronized public State getState() {
            return state;
        }

        synchronized protected void setState(State state) {
            if (this.state != State.ERROR) {
                if (this.state != state) {
                    setChanged();
                }
                this.state = state;
                notifyObservers(this);
            }

        }

        synchronized protected void clearError() {
            if (state == State.ERROR) {
                this.state = State.IDLE;
                setChanged();
                notifyObservers(this);
            }
        }

        synchronized public String getErrorDetail() {
            return "ErrorDetail{" + "code=" + error + ", data=" + errorMsg + '}';
        }

        synchronized protected void setError(Error e, String msg) {
            this.error = e;
            this.errorMsg = msg;
        }

        synchronized public String name() {
            return state.name();
        }
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency);

    public Integer getCurrency();

    public Map<Integer, Integer> getCurrentQuantity();

    public Map<Integer, Integer> getDesiredQuantity();

    // TODO: Fix this.
    public boolean cancelDeposit();

    public boolean storeDeposit(Integer sequenceNumber);

    public boolean withdrawDeposit();

    public boolean envelopeDeposit();

    public boolean collect();

    public boolean reset();

    public boolean storingErrorReset();

    public Status getStatus();

    public void addObserver(Observer observer);
}
