/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

/**
 *
 * @author adji
 */
class ManagerCommandStop extends ManagerCommandAbstract {

    public ManagerCommandStop( ManagerStatus status ) {
        super( status );
    }

    @Override
    void execute() {
        gotoNeutral();
        getDevice().close();
    }

    @Override
    boolean mustStop() {
        return true;
    }
}