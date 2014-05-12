/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device.events;

import devices.device.DeviceEvent;
import java.util.EventListener;

/**
 *
 * @author adji
 */
public interface DeviceEventListener extends EventListener {

    public void onDeviceEvent(DeviceEvent counterEvent);
}
