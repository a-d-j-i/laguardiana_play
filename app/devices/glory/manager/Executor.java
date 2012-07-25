/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import play.Logger;

/**
 *
 * @author adji
 */
public class Executor extends Thread {

    public enum ThState {

        NEUTRAL,
        COMMAND_SENT,
        PROCESSING,
        STOPPED,
    }
    private ThState thState = ThState.NEUTRAL;
    // One entry queue
    private ManagerCommandAbstract currentCommand = null;

    public boolean sendCommand( ManagerCommandAbstract cmd ) {
        synchronized ( this ) {
            switch ( thState ) {
                case NEUTRAL:
                    thState = ThState.COMMAND_SENT;
                    currentCommand = cmd;
                    this.notify();
                    return true;
                default:
                    return false;
            }
        }
    }

    public boolean cancelLastCommand() {
        synchronized ( this ) {
            switch ( thState ) {
                case PROCESSING:
                    currentCommand.cancel();
                    return true;
                default:
                    return false;
            }
        }
    }
    @Override
    public void run() {
        ThState state;
        do {
            synchronized ( this ) {
                state = thState;
            }
            switch ( state ) {
                case NEUTRAL:
                    try {
                        this.wait( 1000 );
                    } catch ( InterruptedException ex ) {
                    }
                    break;
                case COMMAND_SENT:
                    ManagerCommandAbstract cmd;
                    synchronized ( this ) {
                        thState = ThState.PROCESSING;
                        cmd = currentCommand;
                    }
                    cmd.execute();
                    synchronized ( this ) {
                        if ( cmd.mustStop() ) {
                            thState = ThState.STOPPED;
                        } else {
                            thState = ThState.NEUTRAL;
                        }
                    }
                    break;
                case STOPPED:
                case PROCESSING:
                default:
                    Logger.error( "CommandExecutor state error %s", state.name() );
                    try {
                        this.wait( 1000 );
                    } catch ( InterruptedException ex ) {
                    }
                    break;
            }
        } while ( state != ThState.STOPPED );
    }
}
