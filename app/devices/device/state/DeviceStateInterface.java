/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device.state;

import devices.device.task.DeviceTaskInterface;

/**
 *
 * @author adji
 */
public interface DeviceStateInterface {

    public DeviceStateInterface init();

    public DeviceStateInterface step();

    public DeviceStateInterface call(DeviceTaskInterface task);

}
