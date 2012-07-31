package devices;

import devices.glory.Glory;
import devices.glory.manager.Manager;
import java.io.IOException;
import java.util.HashMap;
import play.Logger;
import play.PlayPlugin;

/**
 * *
 *
 * @author adji Some day it could be a real factory with real generic counters,
 * etc. Now is just a static singleton to maintain the reference to the glory
 * instance.
 */
public class CounterFactory extends PlayPlugin {

    static HashMap<String, Glory> devices = new HashMap();
    static HashMap<Glory, Manager> managers = new HashMap();

    static synchronized public Glory getCounter() {
        return getCounter( null );
    }

    public static Manager getManager( String port ) {
        Glory glory = getCounter( port );

        if ( glory == null ) {
            return null;
        }
        if ( managers.containsKey( glory ) ) {
            return managers.get( glory );
        }
        Manager m = new Manager( glory );
        managers.put( glory, m );
        return m;
    }

    synchronized public static Glory getCounter( String port ) {
        if ( port == null ) {
            port = "0";
        }
        if ( devices.containsKey( port ) ) {
            return devices.get( port );
        }

        try {
            Logger.info( String.format( "Configuring serial port %s", port ) );
            SerialPortAdapter serialPort = new SerialPortAdapter( port );
            Logger.info( String.format( "Configuring glory" ) );
            Glory device = new Glory( serialPort );
            devices.put( port, device );
            return device;
        } catch ( IOException e ) {
            Logger.error( "Error opening the serial port" );
            return null;
        }
    }

    static synchronized public void closeAll() {
        for ( Manager m : managers.values() ) {
            m.close();
        }
        for ( Glory g : devices.values() ) {
            g.close();
        }
    }

    @Override
    public void onApplicationStop() {
        Logger.error( "Close all ports" );
        closeAll();
    }
}
