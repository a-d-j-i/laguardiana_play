/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device.state;

import devices.device.DeviceAbstract.DeviceStateApi;
import devices.device.operation.DeviceOperationInterface;
import devices.device.response.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public interface DeviceStateInterface {

    public DeviceStateApi getApi();

    public DeviceStateInterface init();

    public DeviceStateInterface step();

    // Executed by the inner thread, return null if not ready
    public DeviceResponseInterface sendDeviceOperation(DeviceOperationInterface operation, boolean debug);
}
