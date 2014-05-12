/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machines;

import devices.device.DeviceEvent;

/**
 *
 * @author adji
 */
public class P500_MEI extends Machine {

    enum P500_MEI_DEVICES implements DeviceDescription {

        P500_MEI_DEVICE_OS_PRINTER(DeviceType.OS_PRINTER),
        P500_MEI_DEVICE_IO_BOARD_MX220_1_0(DeviceType.IO_BOARD_MX220_1_0),
        P500_MEI_DEVICE_MEI_EBDS(DeviceType.MEI_EBDS);

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
            case P500_MEI_DEVICE_MEI_EBDS:
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
