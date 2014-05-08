package machines;

import devices.DeviceAbstract;
import devices.DeviceInterface;
import devices.DeviceEventListener;
import devices.glory.GloryDE50Device;
import devices.mei.MeiEbds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class Machine implements DeviceEventListener {

    public enum MachineType {

        P500() {
                    @Override
                    Machine getMachineInstance() {
                        return new P500();
                    }
                },
        P500_MEI {

                    @Override
                    Machine getMachineInstance() {
                        return new P500_MEI();
                    }
                },;

        @Override
        public String toString() {
            return name();
        }

        abstract Machine getMachineInstance();

        static public MachineType getMachineType(String machineType) throws IllegalArgumentException {
            return MachineType.valueOf(machineType.toUpperCase());
        }
    };

    public enum DeviceType {

        OS_PRINTER() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return null;
                    }
                },
        IO_BOARD_V4520_1_0() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return null;
                    }
                },
        IO_BOARD_V4520_1_2() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return null;
                    }
                },
        IO_BOARD_MX220_1_0() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return null;
                    }
                },
        GLORY_DE50() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return new GloryDE50Device(deviceDesc);
                    }
                },
        MEI_EBDS() {
                    @Override
                    public DeviceAbstract createDevice(DeviceDescription deviceDesc) {
                        return new MeiEbds(deviceDesc);
                    }
                };

        public enum DeviceClass {

            DEVICE_CLASS_PRINTER,
            DEVICE_CLASS_COUNTER,
            DEVICE_CLASS_IOBOARD,
        };

        abstract public DeviceAbstract createDevice(DeviceDescription deviceDesc);

        @Override
        public String toString() {
            return name();
        }

    };

    static private Machine instance = null;

    // TODO: Check singleton implementation.
    static synchronized public Machine getInstance() {
        if (instance == null) {
            MachineType machineType = MachineType.getMachineType(Configuration.getMachineType());
            instance = machineType.getMachineInstance();
        }
        return instance;
    }

    static public List<DeviceInterface> getDevices() {
        return new ArrayList<DeviceInterface>(getInstance().devices.values());
    }

    public static DeviceInterface findDeviceById(Integer deviceId) {
        return getInstance().devices.get(deviceId);
    }


    /*
     static HashMap<String, Glory> gloryDevices = new HashMap();
     static HashMap<Glory, GloryManager.CounterFactoryApi> gloryManagers = new HashMap();
     static HashMap<String, IoBoard> ioBoardDevices = new HashMap();
     static HashMap<String, Printer> openPrinters = new HashMap();

     public static Printer getPrinter(String port) {

     if (openPrinters.containsKey(port)) {
     return openPrinters.get(port);
     }
     Printer printer = new Printer(port);
     openPrinters.put(port, printer);
     printer.startStatusThread();
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
     for (Printer p : openPrinters.values()) {
     Logger.debug("Closing printer %s", p.getPort());
     p.close();
     }
     }
     */
    public interface DeviceDescription {

        public DeviceType getType();

        public String getMachineId();

    }
    private final Map<Integer, DeviceInterface> devices = new HashMap<Integer, DeviceInterface>();

    abstract protected DeviceDescription[] getDevicesDesc();

    public void start() {
        for (DeviceDescription desc : getDevicesDesc()) {
            DeviceInterface d = desc.getType().createDevice(desc);
            Logger.debug("Start device %s", d);
            d.addEventListener(this);
            d.start();
            devices.put(d.getDeviceId(), d);
            Logger.debug("Start device %s done", d);
        }
    }

    public void stop() {
        for (DeviceInterface d : devices.values()) {
            Logger.debug("Stop device %s", d.toString());
            d.stop();
            Logger.debug("Stop device %s done", d.toString());
        }
    }
}
