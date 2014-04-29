/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machines;

import devices.Device;
import devices.Device.DeviceDesc;
import java.util.ArrayList;
import java.util.List;
import models.Configuration;
import models.ModelFacade;
import models.db.LgDevice;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class Machine {

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

    static public Machine createMachine() {
        MachineType machineType = MachineType.getMachineType(Configuration.getMachineType());
        Machine machine = machineType.getMachineInstance();
        machine.start();
        return machine;
    }

    public List<LgDevice> getDevices() {
        List<LgDevice> ret = new ArrayList<LgDevice>();
        for (Device d : devices) {
            ret.add(d.getLgDevice());
        }
        return ret;
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
    private final List<Device> devices = new ArrayList<Device>();

    abstract protected DeviceDesc[] getDevicesDesc();

    public void start() {
        for (DeviceDesc desc : getDevicesDesc()) {
            Device d = desc.CreateDevice();
            Logger.debug("Start device %s", d);
            d.addListener(ModelFacade.getDeviceListener());
            d.start();
            devices.add(d);
            Logger.debug("Start device %s done", d);
        }
    }

    public void stop() {
        for (Device d : devices) {
            Logger.debug("Stop device %s", d.toString());
            d.stop();
            Logger.debug("Stop device %s done", d.toString());
        }
    }
}
