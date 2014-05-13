/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory;

import devices.device.DeviceStatus;
import devices.device.task.DeviceTaskInterface;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50DeviceStateApi {

    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    boolean closing = false;
    final private int GLORY_READ_TIMEOUT = 5000;
    final private GloryDE50 gl = new GloryDE50(GLORY_READ_TIMEOUT);
    final BlockingQueue<DeviceTaskInterface> queue;

    GloryDE50DeviceStateApi(BlockingQueue<DeviceTaskInterface> operationQueue) {
        this.queue = operationQueue;
    }

    public String sendGloryDE50Operation(GloryDE50OperationInterface operation, final GloryDE50OperationResponse response) {
        return sendGloryDE50Operation(operation, false, response);
    }

    public String sendGloryDE50Operation(GloryDE50OperationInterface operation, boolean debug, final GloryDE50OperationResponse response) {
        return gl.sendOperation(operation, debug, response);
    }

    public void notifyListeners(String details) {
    }

    public void notifyListeners(DeviceStatus.STATUS status) {
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean b) {
        closing = b;
    }

    public boolean open(String value) {
        Logger.debug("api open");
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(value, portConf);
        Logger.info(String.format("Configuring serial port %s", serialPort));
        gl.close();
        Logger.debug("Glory port open try");
        boolean ret = gl.open(serialPort);
        Logger.debug("Glory port open : %s", ret ? "success" : "fails");
        return ret;
    }

    void close() {
        gl.close();
    }

    public DeviceTaskInterface poll(int timeoutMS, TimeUnit timeUnit) throws InterruptedException {
        return queue.poll(timeoutMS, timeUnit);
    }

}
