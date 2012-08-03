package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.command.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class Manager {

    final private Thread thread;
    final private Glory device;
    final private ManagerThreadState managerThreadState;
    // TODO: Better error reporting, as a class with arguments.
    final private AtomicReference<String> error = new AtomicReference<String>();
    final private AtomicReference<String> success = new AtomicReference<String>();

    public Manager( Glory device ) {
        this.device = device;
        this.managerThreadState = new ManagerThreadState();
        this.thread = new Thread( new ManagerThread( new ThreadCommandApi() ) );
    }
    /*
     *
     *
     * Thread API
     *
     *
     */

    public class ThreadCommandApi {

        private final ManagerThreadState.ThreadApi managerThreadApi;

        public ThreadCommandApi() {
            managerThreadApi = managerThreadState.getThreadApi();
        }

        public ManagerCommandAbstract getCurrentCommand() {
            return managerThreadApi.getCurrentCommand();
        }

        boolean mustStop() {
            return managerThreadApi.mustStop();
        }

        void stopped() {
            managerThreadApi.stopped();
        }

        public GloryCommandAbstract sendGloryCommand( GloryCommandAbstract cmd ) {
            return device.sendCommand( cmd );
        }

        public void setError( String e ) {
            error.set( e );
        }

        public void compareAndSetError( String expect, String update ) {
            error.compareAndSet( expect, update );
        }

        public void setSuccess( String successMsg ) {
            success.set( successMsg );
        }
    }

    /*
     *
     *
     * Controller API
     *
     *
     */
    public class ControllerApi {

        private final ManagerThreadState.ControllerApi managerControllerApi;
        private final ThreadCommandApi threadCommandApi = new ThreadCommandApi();

        public ControllerApi() {
            managerControllerApi = managerThreadState.getControllerApi();
        }

        public boolean count( Map<Integer, Integer> desiredQuantity ) {
            if ( managerControllerApi.getCurrentCommand() != null ) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand( new Count( threadCommandApi, desiredQuantity ) );
        }

        public Map<Integer, Integer> getCurrentQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if ( cmd == null ) {
                return null;
            }
            if ( !( cmd instanceof Count ) ) {
                return null;
            }
            return ( ( Count ) cmd ).getCurrentQuantity();
        }

        public Map<Integer, Integer> getDesiredQuantity() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if ( cmd == null ) {
                return null;
            }
            if ( !( cmd instanceof Count ) ) {
                return null;
            }
            return ( ( Count ) cmd ).getDesiredQuantity();
        }

        public boolean cancelDeposit() {
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if ( cmd == null ) {
                return managerControllerApi.sendCommand( new CancelCount( threadCommandApi ) );
            }
            if ( !( cmd instanceof Count ) ) {
                return false;
            }
            cmd.cancel();
            return true;
        }

        public boolean storeDeposit( int sequenceNumber ) {
            Logger.debug( "storeDeposit" );
            ManagerCommandAbstract cmd = managerControllerApi.getCurrentCommand();
            if ( cmd == null ) {
                return true;
            }
            if ( !( cmd instanceof Count ) ) {
                return false;
            }
            ( ( Count ) cmd ).storeDeposit( sequenceNumber );
            return true;
        }

        public boolean reset() {
            Logger.debug( "------reset" );
            if ( managerControllerApi.getCurrentCommand() != null ) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand( new Reset( threadCommandApi ) );
        }

        public boolean storingErrorReset() {
            Logger.debug( "------storing error reset" );
            if ( managerControllerApi.getCurrentCommand() != null ) {
                // still executing
                return false;
            }
            return managerControllerApi.sendCommand( new StoringErrorReset( threadCommandApi ) );
        }

        public String getError() {
            return error.get();
        }

        public String getSuccess() {
            return success.get();
        }
    }
    /*
     *
     *
     * Counter Factory API
     *
     *
     */

    public class CounterFactoryApi {

        public void startThread() {
            thread.start();
        }

        public void close() {
            Logger.debug( "Closing Manager Stop" );
            managerThreadState.stop();
            try {
                Logger.debug( "Closing Manager waitUntilStop" );
                managerThreadState.waitUntilStop( 60000 );
                thread.join( 1000 );
            } catch ( InterruptedException ex ) {
                Logger.info( "Manager thread don't die" );
            }
            Logger.debug( "Closing Manager done" );
        }

        public ControllerApi getControllerApi() {
            return new ControllerApi();
        }
    }

    public CounterFactoryApi getCounterFactoryApi() {
        return new CounterFactoryApi();
    }
}
