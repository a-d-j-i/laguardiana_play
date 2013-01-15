package devices.glory.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

/**
 *
 * @author adji
 */
public class FakeGloryManager implements ManagerInterface {

    final private static ManagerInterface.Status status = new ManagerInterface.Status();
    static Map<Integer, Integer> desiredQuantity = null;
    static Integer currency;
    static int counter = 0;
    static boolean billDeposit = true;
    static boolean stepOneDone = false;

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        status.setState(State.NEUTRAL);
        billDeposit = true;
        FakeGloryManager.desiredQuantity = desiredQuantity;
        FakeGloryManager.currency = currency;
        counter = 0;
        stepOneDone = false;
        return true;
    }

    public boolean envelopeDeposit() {
        billDeposit = false;
        status.setState(State.NEUTRAL);
        counter = 0;
        return true;
    }

    public Integer getCurrency() {
        return currency;
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (int i = 0; i < 64; i++) {
//            ret.put(i, (int) (Math.random() * 100));
            ret.put(i, counter);
        }
        return ret;
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    public void cancelCommand() {
        status.setState(State.CANCELING);
        status.setState(State.NEUTRAL);
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        if (status.getState() != State.CANCELING && status.getState() != State.NEUTRAL) {
            if (status.getState() == State.ESCROW_FULL) {
                status.setState(State.STORING);
                status.setState(State.COUNTING);
            } else {
                status.setState(State.STORING);
                status.setState(State.PUT_THE_BILLS_ON_THE_HOPER);
            }
        }
        return true;
    }

    public boolean withdrawDeposit() {
        status.setState(State.NEUTRAL);
        return true;
    }

    public boolean collect() {
        status.setState(State.NEUTRAL);
        return true;
    }

    public boolean reset() {
        status.setState(State.NEUTRAL);
        return true;
    }

    public boolean storingErrorReset() {
        status.setState(State.NEUTRAL);
        return true;
    }

    public ManagerInterface.Status getStatus() {
        counter++;
        if (counter % 10 == 0) {
            if (billDeposit) {
                if (!stepOneDone) {
                    status.setState(State.ESCROW_FULL);
                    stepOneDone = true;
                }
            } else {
                status.setState(State.PUT_THE_ENVELOPE_IN_THE_ESCROW);
            }
        }
        if (counter % 20 == 0) {
            status.setState(State.READY_TO_STORE);
        }

        if (counter == 40) {
            status.setState(State.PUT_THE_BILLS_ON_THE_HOPER);
            counter = 0;
        }

        return status;
    }

    public void addObserver(Observer observer) {
        status.addObserver(observer);
    }
}
