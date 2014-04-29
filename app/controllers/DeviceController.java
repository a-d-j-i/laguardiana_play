package controllers;

import java.util.List;
import models.ModelFacade;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

@With({Secure.class})
public class DeviceController extends Controller {

    public static void list() {
        renderArgs.put("data", ModelFacade.getDevices());
        flash.put("backUrl", request.url);
        render();
    }

    public static void commands(Integer deviceId) {
        if (deviceId == null) {
            list();
        }
        LgDevice d = LgDevice.findById(deviceId);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.deviceType.name() + ".html");
    }

    // TODO: Enforce security.
    public static void properties(Integer deviceId) {
        if (deviceId == null) {
            list();
        }
        LgDevice d = LgDevice.findById(deviceId);
        if (d == null) {
            list();
        }
        List<LgDeviceProperty> data = LgDeviceProperty.getEditables(d);
        /*for (LgDeviceProperty p : data) {
         //boolean perm = Secure.checkPermission(p.name, "GET");
         if (!perm) {
         data.remove(p);
         }
         }*/
        Logger.debug("data %s -> %s", d, data);
        renderArgs.put("data", data);
        renderArgs.put("device", d);
        render();
    }

    public static void setProperty(Integer deviceId, String property, String value) {
        if (!request.isAjax()) {
            list();
        }
        boolean perm = Secure.checkPermission(property, "SET");
        Object ret[] = new Object[3];
        if (!perm) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("access denied for property %s", property);
            renderJSON(ret);
        }

        Logger.debug("set system property %s to %s for deviceId", property, value, deviceId);
        if (deviceId == null) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("invalid id %d", deviceId);
            renderJSON(ret);
        }
        LgDevice d = LgDevice.findById(deviceId);
        if (d == null) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("device not found %d", deviceId);
            renderJSON(ret);
        }
        LgDeviceProperty p = LgDeviceProperty.getProperty(d, property);
        if (p == null) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("invalid property %s", property);
            renderJSON(ret);
        } else {
            ret[0] = p.devicePropertyId;
            ret[1] = p.value;
            ret[2] = false;
            switch (p.editType) {
                case BOOLEAN:
                    if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                        p.value = "false";
                        p.save();
                    } else if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        p.value = "true";
                        p.save();
                    } else {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case INTEGER:
                    try {
                        Integer i = Integer.parseInt(value);
                        p.value = i.toString();
                        p.save();
                    } catch (NumberFormatException e) {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case STRING:
                    p.value = value;
                    p.save();
                    break;
                case NOT_EDITABLE:
                default:
                    ret[2] = String.format("Property %s not editable", property);
                    break;
            }
            renderJSON(ret);
        }
    }

}
