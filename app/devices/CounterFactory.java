package devices;

import devices.glory.Glory;
import devices.glory.manager.Manager;
import java.io.IOException;
import java.util.HashMap;
import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * Glory and Glory Manager factory. Plugin to close all ports on app shutdown.
 *
 * @author adji
 */
public class CounterFactory extends PlayPlugin {

    static HashMap<String, Glory> devices = new HashMap();
    static HashMap<Glory, Manager.CounterFactoryApi> managers = new HashMap();

    static synchronized public Glory getCounter() {
        return getCounter(null);
    }

    public static Manager.ControllerApi getGloryManager() {
        return getManager(Play.configuration.getProperty("glory.port"));
    }

    public static Manager.ControllerApi getManager(String port) {
        Glory glory = getCounter(port);

        if (glory == null) {
            return null;
        }
        if (managers.containsKey(glory)) {
            return managers.get(glory).getControllerApi();
        }
        Manager m = new Manager(glory);
        Manager.CounterFactoryApi mcf = m.getCounterFactoryApi();
        mcf.startThread();
        managers.put(glory, mcf);
        return mcf.getControllerApi();
    }

    synchronized public static Glory getCounter(String port) {
        if (port == null) {
            port = "0";
        }
        if (devices.containsKey(port)) {
            return devices.get(port);
        }

        try {
            Logger.info(String.format("Configuring serial port %s", port));
            //SerialPortAdapterInterface serialPort = new SerialPortAdapterJSSC( port );
            SerialPortAdapterInterface serialPort = new SerialPortAdapterRxTx(port);
            Logger.info(String.format("Configuring glory"));
            Glory device = new Glory(serialPort);
            devices.put(port, device);
            return device;
        } catch (IOException e) {
            Logger.error("Error opening the serial port");
            return null;
        }
    }

    static synchronized public void closeAll() {
        for (Manager.CounterFactoryApi m : managers.values()) {
            Logger.debug("Closing Manager");
            m.close();
        }
        for (Glory g : devices.values()) {
            Logger.debug("Closing Device");
            g.close();
        }
    }

    @Override
    public void onApplicationStop() {
        Logger.debug("Close all ports");
        closeAll();
    }
}
