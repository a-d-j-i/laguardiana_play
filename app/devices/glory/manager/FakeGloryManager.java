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
        status.setState(ManagerState.NEUTRAL);
        billDeposit = true;
        FakeGloryManager.desiredQuantity = desiredQuantity;
        FakeGloryManager.currency = currency;
        counter = 0;
        stepOneDone = false;
        return true;
    }

    public boolean envelopeDeposit() {
        billDeposit = false;
        status.setState(ManagerState.NEUTRAL);
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
        status.setState(ManagerState.CANCELING);
        status.setState(ManagerState.NEUTRAL);
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        if (status.getState() != ManagerState.CANCELING && status.getState() != ManagerState.NEUTRAL) {
            if (status.getState() == ManagerState.ESCROW_FULL) {
                status.setState(ManagerState.STORING);
                status.setState(ManagerState.COUNTING);
            } else {
                status.setState(ManagerState.STORING);
                status.setState(ManagerState.PUT_THE_BILLS_ON_THE_HOPER);
            }
        }
        return true;
    }

    public boolean withdrawDeposit() {
        status.setState(ManagerState.NEUTRAL);
        return true;
    }

    public boolean collect() {
        status.setState(ManagerState.NEUTRAL);
        return true;
    }

    public boolean reset() {
        status.setState(ManagerState.NEUTRAL);
        return true;
    }

    public boolean storingErrorReset() {
        status.setState(ManagerState.NEUTRAL);
        return true;
    }

    public ManagerInterface.Status getStatus() {
        counter++;
        if (counter % 10 == 0) {
            if (billDeposit) {
                if (!stepOneDone) {
                    status.setState(ManagerState.ESCROW_FULL);
                    stepOneDone = true;
                }
            } else {
                status.setState(ManagerState.PUT_THE_ENVELOPE_IN_THE_ESCROW);
            }
        }
        if (counter % 20 == 0) {
            status.setState(ManagerState.READY_TO_STORE);
        }

        if (counter == 40) {
            status.setState(ManagerState.PUT_THE_BILLS_ON_THE_HOPER);
            counter = 0;
        }

        return status;
    }

    public void addObserver(Observer observer) {
        status.addObserver(observer);
    }
}
