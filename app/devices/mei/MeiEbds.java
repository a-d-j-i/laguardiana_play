/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.Device;
import devices.DeviceClassCounter;
import devices.DeviceState;
import java.util.Map;
import models.db.LgDeviceProperty;

/**
 *
 * @author adji
 */
public class MeiEbds extends Device implements DeviceClassCounter {

    public MeiEbds(DeviceType deviceType, String machineDeviceId) {
        super(deviceType, machineDeviceId);
    }

    @Override
    protected void CheckProperties() {
        LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
    }

    @Override
    public void mainLoop() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }

    @Override
    public DeviceState getState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean envelopeDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean collect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storingErrorReset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Integer getCurrency() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void cancelCommand() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean withdrawDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
