/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

/**
 *
 * @author adji
 */
public class ManagerCommandDeposit extends ManagerCommandAbstract {

    public ManagerCommandDeposit( ManagerStatus status ) {
        super( status );
    }

    @Override
    void execute() {

        if ( !gotoNeutral() ) {
            return;
        }
        if ( !sendGloryCommand( new devices.glory.command.SetDepositMode() ) ) {
            return;
        }
        int[] bills = new int[ 32 ];
        if ( !sendGloryCommand( new devices.glory.command.BatchDataTransmition( bills ) ) ) {
            return;
        }
        Sense();

    }
}
