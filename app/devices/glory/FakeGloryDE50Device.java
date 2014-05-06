package devices.glory;

/**
 *
 * @author adji
 */
public class FakeGloryDE50Device {
    /*
     final private static ManagerInterface.State state = new ManagerInterface.State();
     static Map<Integer, Integer> desiredQuantity = null;
     static Integer currency;
     static int counter = 0;
     static boolean billDeposit = true;
     static boolean stepOneDone = false;

     public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
     state.setState(MANAGER_STATE.NEUTRAL);
     billDeposit = true;
     FakeDevice.desiredQuantity = desiredQuantity;
     FakeDevice.currency = currency;
     counter = 0;
     stepOneDone = false;
     return true;
     }

     public boolean envelopeDeposit() {
     billDeposit = false;
     state.setState(MANAGER_STATE.NEUTRAL);
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
     state.setState(MANAGER_STATE.CANCELING);
     state.setState(MANAGER_STATE.NEUTRAL);
     }

     public boolean storeDeposit(Integer sequenceNumber) {
     if (getStatus().getState() != MANAGER_STATE.CANCELING && getStatus().getState() != MANAGER_STATE.NEUTRAL) {
     if (getStatus().getState() == MANAGER_STATE.ESCROW_FULL) {
     state.setState(MANAGER_STATE.STORING);
     state.setState(MANAGER_STATE.COUNTING);
     } else {
     state.setState(MANAGER_STATE.STORING);
     state.setState(MANAGER_STATE.PUT_THE_BILLS_ON_THE_HOPER);
     }
     }
     return true;
     }

     public boolean withdrawDeposit() {
     state.setState(MANAGER_STATE.NEUTRAL);
     return true;
     }

     public boolean collect() {
     state.setState(MANAGER_STATE.NEUTRAL);
     return true;
     }

     public boolean reset() {
     state.setState(MANAGER_STATE.NEUTRAL);
     return true;
     }

     public boolean storingErrorReset() {
     state.setState(MANAGER_STATE.NEUTRAL);
     return true;
     }

     public ManagerStatus getStatus() {
     counter++;
     if (counter % 10 == 0) {
     if (billDeposit) {
     if (!stepOneDone) {
     state.setState(MANAGER_STATE.ESCROW_FULL);
     stepOneDone = true;
     }
     } else {
     state.setState(MANAGER_STATE.PUT_THE_ENVELOPE_IN_THE_ESCROW);
     }
     }
     if (counter % 20 == 0) {
     state.setState(MANAGER_STATE.READY_TO_STORE);
     }

     if (counter == 40) {
     state.setState(MANAGER_STATE.PUT_THE_BILLS_ON_THE_HOPER);
     counter = 0;
     }

     return new ManagerStatus(state);
     }

     public void addObserver(Observer observer) {
     state.addObserver(observer);
     }

     public GloryDE50 getCounter() {
     return null;
     }
     */
}
