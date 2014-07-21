/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class GloryDE50Status implements DeviceStatusInterface {

    public boolean is(Enum type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean is(Class type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    static public enum GloryDE50StatusType {

        ERROR,
        OPEN_PORT,
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
        CANCELING,
        BAG_COLLECTED,
        JAM;

    };

    public Enum getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
