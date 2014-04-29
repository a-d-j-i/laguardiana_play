/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machines;

import devices.Device.DeviceDesc;
import static devices.Device.DeviceType.*;

/**
 *
 * @author adji
 */
public class P500 extends Machine {

    @Override
    protected DeviceDesc[] getDevicesDesc() {
        return new DeviceDesc[]{
            new DeviceDesc(OS_PRINTER, "P500_OS_PRINTER"),
            new DeviceDesc(IO_BOARD_MX220_1_0, "P500_IO_BOARD"),
            new DeviceDesc(GLORY_DE50, "P500_GLORY"),};
    }
}
