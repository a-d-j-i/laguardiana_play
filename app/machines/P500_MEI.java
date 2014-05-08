/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machines;

import devices.DeviceEvent;
import static machines.Machine.DeviceType.GLORY_DE50;
import static machines.Machine.DeviceType.IO_BOARD_MX220_1_0;
import static machines.Machine.DeviceType.OS_PRINTER;
import machines.P500.P500_DEVICES;
import static machines.P500.P500_DEVICES.P500_DEVICE_GLORY_DE50;
import static machines.P500.P500_DEVICES.P500_DEVICE_IO_BOARD_MX220_1_0;
import static machines.P500.P500_DEVICES.P500_DEVICE_OS_PRINTER;

/**
 *
 * @author adji
 */
public class P500_MEI extends Machine {

    enum P500_MEI_DEVICES implements DeviceDescription {

        P500_MEI_DEVICE_OS_PRINTER(OS_PRINTER),
        P500_MEI_DEVICE_IO_BOARD_MX220_1_0(IO_BOARD_MX220_1_0),
        P500_MEI_DEVICE_GLORY_DE50(GLORY_DE50);

        private final DeviceType type;

        private P500_MEI_DEVICES(DeviceType type) {
            this.type = type;
        }

        public DeviceType getType() {
            return type;
        }

        public String getMachineId() {
            return name();
        }
    };

    @Override
    protected DeviceDescription[] getDevicesDesc() {
        return P500_MEI_DEVICES.values();
    }

    public void onDeviceEvent(DeviceEvent deviceEvent) {
        // Switch by device.
        switch ((P500_MEI_DEVICES) deviceEvent.getSourceDevice()) {
            case P500_MEI_DEVICE_OS_PRINTER:
                break;
            case P500_MEI_DEVICE_IO_BOARD_MX220_1_0:
                break;
            case P500_MEI_DEVICE_GLORY_DE50:
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}