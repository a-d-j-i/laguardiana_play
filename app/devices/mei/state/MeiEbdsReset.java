/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.mei.MeiEbdsDeviceStateApi;

/**
 *
 * @author adji
 */
public class MeiEbdsReset extends MeiEbdsStateOperation {

    final MeiEbdsStateOperation prevStep;

    public MeiEbdsReset(MeiEbdsDeviceStateApi api, MeiEbdsStateOperation prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public DeviceStateInterface step() {
        return new MeiEbdsStateMain(api);
    }
}
