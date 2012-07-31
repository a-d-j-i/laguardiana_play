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
public class CancelDeposit extends ManagerCommandAbstract {

    @Override
    public void execute( ManagerThread thread ) {
        gotoNeutral( thread );
    }
}
