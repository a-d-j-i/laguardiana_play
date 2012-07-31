/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.ManagerThread;

/**
 *
 * @author adji
 */
public class StoreDeposit extends ManagerCommandAbstract {

    private int sequenceNumber;

    public StoreDeposit( int sequenceNumber ) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public void execute( ManagerThread thread ) {
        sendGloryCommand( thread, new devices.glory.command.CountingDataRequest() );
        boolean haveBills = thread.getStatus().setDepositedBillDataFromGlory();
        if ( haveBills ) {
            if ( !sendGloryCommand( thread, new devices.glory.command.StoringStart( sequenceNumber ) ) ) {
                return;
            }
            thread.getStatus().depositOk();
        }
        gotoNeutral( thread );
    }
}
