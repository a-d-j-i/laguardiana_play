package controllers;

import devices.DeviceInterface;
import java.util.List;
import machines.Machine;
import models.db.LgDeviceProperty;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

@With({Secure.class})
public class DeviceController extends Controller {

    public static void list() {
        renderArgs.put("data", Machine.getDevices());
        flash.put("backUrl", request.url);
        render();
    }

    public static void commands(Integer deviceId) {
        if (deviceId == null) {
            list();
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        renderArgs.put("device", d);
        renderArgs.put("backUrl", flash.get("backUrl"));
        render("DeviceController/" + d.getName().toUpperCase() + "_OPERATIONS.html");
    }

    // TODO: Enforce security.
    public static void properties(Integer deviceId) {
        if (deviceId == null) {
            list();
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d == null) {
            list();
        }
        List<LgDeviceProperty> data = d.getEditableProperties();
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
        ret[0] = null;
        ret[1] = null;
        if (!perm) {
            ret[2] = String.format("access denied for property %s", property);
            renderJSON(ret);
        }
        DeviceInterface d = Machine.findDeviceById(deviceId);
        if (d == null) {
            ret[2] = String.format("invalid device Id");
            renderJSON(ret);
        }
        LgDeviceProperty newProp = d.setProperty(property, value);
        if (newProp == null) {
            ret[2] = String.format("invalid property %s", property);
            renderJSON(ret);
        } else {
            ret[0] = newProp.devicePropertyId;
            ret[1] = newProp.value;
            ret[2] = false;
            switch (newProp.editType) {
                case BOOLEAN:
                    if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                        newProp.value = "false";
                        newProp.save();
                    } else if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        newProp.value = "true";
                        newProp.save();
                    } else {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case INTEGER:
                    try {
                        Integer i = Integer.parseInt(value);
                        newProp.value = i.toString();
                        newProp.save();
                    } catch (NumberFormatException e) {
                        ret[2] = String.format("Invalid value '%s' for property %s", value, property);
                    }
                    break;
                case STRING:
                    newProp.value = value;
                    newProp.save();
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
