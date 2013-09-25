package devices;

import devices.SerialPortAdapterAbstract.PORTBITS;
import devices.SerialPortAdapterAbstract.PORTPARITY;
import devices.SerialPortAdapterAbstract.PORTSPEED;
import devices.SerialPortAdapterAbstract.PORTSTOPBITS;
import devices.SerialPortAdapterAbstract.PortConfiguration;
import devices.glory.Glory;
import devices.glory.manager.GloryManager;
import devices.glory.manager.ManagerInterface;
import devices.ioboard.IoBoard;
import devices.ioboard.IoBoard.IOBOARD_VERSION;
import devices.printer.Printer;
import java.util.HashMap;
import play.Logger;
import play.PlayPlugin;

/**
 * Glory and Glory Manager factory. Plugin to close all ports on app shutdown.
 *
 * @author adji
 */
public class DeviceFactory extends PlayPlugin {

    static HashMap<String, Glory> gloryDevices = new HashMap();
    static HashMap<Glory, GloryManager.CounterFactoryApi> gloryManagers = new HashMap();
    static HashMap<String, IoBoard> ioBoardDevices = new HashMap();
    static Printer printer = null;

    public static Printer getPrinter(String port) {
        if (printer == null) {
            printer = new Printer(port);
            printer.startStatusThread();
        }
        return printer;
    }

    public static ManagerInterface getGloryManager(String port) {
        Glory glory = getCounter(port);

        if (glory == null) {
            return null;
        }
        if (gloryManagers.containsKey(glory)) {
            return gloryManagers.get(glory).getControllerApi();
        }
        GloryManager m = new GloryManager(glory);
        GloryManager.CounterFactoryApi mcf = m.getCounterFactoryApi();
        mcf.startThread();
        gloryManagers.put(glory, mcf);
        return mcf.getControllerApi();
    }

    synchronized public static Glory getCounter(String port) {
        if (port == null) {
            port = "0";
        }
        if (gloryDevices.containsKey(port)) {
            return gloryDevices.get(port);
        }
        Logger.info(String.format("Configuring glory on serial port %s", port));
        //SerialPortAdapterInterface serialPort = new SerialPortAdapterJSSC( port );
        PortConfiguration gloryPortConf = new PortConfiguration(PORTSPEED.BAUDRATE_9600, PORTBITS.BITS_7, PORTSTOPBITS.STOP_BITS_1, PORTPARITY.PARITY_EVEN);
        SerialPortAdapterInterface serialPort = new SerialPortAdapterRxTx(port, gloryPortConf);
        Logger.info(String.format("Configuring glory"));
        Glory device = new Glory(serialPort);
        gloryDevices.put(port, device);
        return device;
    }

    synchronized public static IoBoard getIoBoard(String port, IOBOARD_VERSION version) {
        if (port == null) {
            port = "0";
        }
        if (ioBoardDevices.containsKey(port)) {
            return ioBoardDevices.get(port);
        }
        Logger.info(String.format("Configuring ioboard on serial port %s", port));
        //SerialPortAdapterInterface serialPort = new SerialPortAdapterJSSC( port );
        PortConfiguration iBoardPortConf = new PortConfiguration(version.getBaudRate(), PORTBITS.BITS_8, PORTSTOPBITS.STOP_BITS_1, PORTPARITY.PARITY_NONE);
        SerialPortAdapterInterface serialPort = new SerialPortAdapterRxTx(port, iBoardPortConf);
        Logger.info(String.format("Configuring glory"));
        IoBoard device = new IoBoard(serialPort, version);
        device.startStatusThread();
        ioBoardDevices.put(port, device);
        return device;
    }

    static synchronized public void closeAll() {
        for (GloryManager.CounterFactoryApi m : gloryManagers.values()) {
            Logger.debug("Closing Manager");
            m.close();
        }
        for (Glory g : gloryDevices.values()) {
            Logger.debug("Closing glory Device");
            g.close();
        }
        for (IoBoard b : ioBoardDevices.values()) {
            Logger.debug("Closing ioBoard Device");
            b.close();
        }
    }

    @Override
    public void onApplicationStop() {
        Logger.debug("onApplicationStop Close all ports");
        closeAll();
        Logger.debug("onApplicationStop Close all ports DONE");
    }
}
