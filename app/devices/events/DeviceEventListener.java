/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.events;

import devices.DeviceEvent;
import java.util.EventListener;

/**
 *
 * @author adji
 */
public interface DeviceEventListener extends EventListener {

    public void onErrorEvent(ErrorEvent counterEvent);
    public void onCounterEvent(CounterEvent counterEvent);
    public void onDeviceEvent(DeviceEvent counterEvent);
}
