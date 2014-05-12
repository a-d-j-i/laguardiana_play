/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machines;

import devices.device.DeviceEvent;
import machines.Machine.DeviceDescription;
import static machines.P500.P500_DEVICES.P500_DEVICE_OS_PRINTER;

/**
 *
 * @author adji
 */
public class P500 extends Machine {

    enum P500_DEVICES implements DeviceDescription {

        P500_DEVICE_OS_PRINTER(DeviceType.OS_PRINTER),
        P500_DEVICE_IO_BOARD_MX220_1_0(DeviceType.IO_BOARD_MX220_1_0),
        P500_DEVICE_GLORY_DE50(DeviceType.GLORY_DE50);

        private final DeviceType type;

        private P500_DEVICES(DeviceType type) {
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
        return P500_DEVICES.values();
    }

    public void onDeviceEvent(DeviceEvent deviceEvent) {
        // Switch by device.
        switch ((P500_DEVICES) deviceEvent.getSourceDevice()) {
            case P500_DEVICE_OS_PRINTER:
                break;
            case P500_DEVICE_IO_BOARD_MX220_1_0:
                break;
            case P500_DEVICE_GLORY_DE50:
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
