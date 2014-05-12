/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device;

import devices.device.events.DeviceEventListener;
import java.util.List;
import models.db.LgDeviceProperty;

/**
 *
 * @author adji
 */
public interface DeviceInterface {

    public void start();

    public void stop();

    public void addEventListener(DeviceEventListener listener);

    public void removeEventListener(DeviceEventListener listener);

    public String getName();

    public Integer getDeviceId();

    public List<LgDeviceProperty> getEditableProperties();

    public LgDeviceProperty setProperty(String property, String value);

    public DeviceStatus getStatus();

    public boolean clearError();
}
