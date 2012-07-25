package models;

import devices.CounterFactory;
import devices.glory.Glory;
import devices.glory.GloryStatus;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.command.CommandWithDataResponse;
import devices.glory.command.GloryCommandAbstract;
import play.Logger;
import play.Play;

/**
 * This class saves in memory the status of the Glory device.
 *
 * @author adji
 */
public class CounterStatus {

    private final Glory glory;
    private static final CounterStatus instance = new CounterStatus();
    private boolean cancel = false;
    private GloryStatus gStatus = null;

    private CounterStatus() {
        glory = CounterFactory.getCounter( Play.configuration.getProperty( "glory.port" ) );
    }

    public static CounterStatus getInstance() {
        return instance;
    }

    public Glory getGlory() {
        return glory;
    }

    public enum CurrentState {

        NONE,
        COUNT,
    }
    private CurrentState currentState = CurrentState.NONE;

    synchronized public CurrentState getCurrentState() {
        return currentState;
    }

    synchronized public boolean setCurrentState( CurrentState cmd ) {
        if ( currentState == CurrentState.NONE ) {
            cancel = false;
            currentState = cmd;
            return true;
        }
        return false;
    }

    synchronized public boolean isCancel() {
        return this.cancel;
    }

    synchronized public void cancel() {
        this.cancel = true;
    }

    synchronized void addError( String string ) {
        Logger.error( string );
    }

    synchronized public String getError() {
        return "must be implemented";
    }

    synchronized boolean isError() {
        return false;
    }

    synchronized public GloryStatus getStatus() {
        return gStatus;
    }

    public void Sense() {
        CommandWithDataResponse cmd = new devices.glory.command.Sense();
        glory.sendCommand( cmd );
        if ( cmd.getError() != null ) {
            addError( cmd.getError() );
        }
        synchronized ( this ) {
            gStatus.setStatus( cmd );
        }
    }

    public boolean sendGloryCommand( GloryCommandAbstract cmd ) {
        CommandWithDataResponse ret = ( CommandWithDataResponse ) glory.sendCommand( cmd );
        if ( ret.getError() != null ) {
            addError( "sendGloryCommand : " + ret.getError() );
            return false;
        }
        Sense();
        return true;
    }

    public boolean gotoNeutral() {
        Sense();
        switch ( getStatus().getD1Mode() ) {
            case initial:
                sendGloryCommand( new devices.glory.command.RemoteCancel() );
                if ( getStatus().getD1Mode() != D1Mode.neutral ) {
                    addError( "cant set neutral mode" );
                    return false;
                }
                break;
            case neutral:
                break;
            default:
                addError( String.format( "Invalid D11 mode %s", getStatus().getD1Mode().name() ) );
                return false;
        }
        return true;
    }
}
