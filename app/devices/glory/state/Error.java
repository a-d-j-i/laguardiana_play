/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import devices.glory.status.GloryDE50DeviceErrorEvent;

/**
 *
 * @author adji
 */
public class Error extends GloryDE50StateAbstract {

    private final String error;

    Error(GloryDE50StateMachineApi api, GloryDE50DeviceErrorEvent.ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
    }

    @Override
    public GloryDE50StateAbstract step() {
        return this;
    }

    public String getError() {
        return error;
    }

}
