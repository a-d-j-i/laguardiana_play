/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import devices.glory.manager.ManagerInterface;
import devices.ioboard.IoBoard;
import devices.printer.Printer;
import java.util.List;
import models.Configuration;
import models.ModelFacade;
import models.db.LgSystemProperty;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Base class for all the controllers that use the counter to deposit.
 *
 * @author adji
 */
@With({Secure.class})
public class ConfigController extends Controller {

    public static void index() {
        List<LgSystemProperty> data = LgSystemProperty.getEditables();
        for (LgSystemProperty p : data) {
            boolean perm = Secure.checkPermission(p.name, "GET");
            if (!perm) {
                data.remove(p);
            }
        }
        renderArgs.put("data", data);
        render();
    }

    public static void setProperty(String property, String value) {
        if (!request.isAjax()) {
            index();
        }
        boolean perm = Secure.checkPermission(property, "SET");
        Object ret[] = new Object[3];
        if (!perm) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("access denied for property %s", property);
            renderJSON(ret);
        }

        Logger.debug("set system property %s to %s", property, value);
        LgSystemProperty p = LgSystemProperty.getProperty(property);
        if (p == null) {
            ret[0] = null;
            ret[1] = null;
            ret[2] = String.format("invalid property %s", property);
            renderJSON(ret);
        } else {
            ret[0] = p.propertyId;
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

    public static void status() {
        ManagerInterface.ManagerStatus gstatus = null;
        String gerror = null;
        Printer.PrinterStatus pstatus = null;
        String ierror = null;

        final ManagerInterface manager = ModelFacade.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getStatus().toString();
        }
        if (!Configuration.isIgnoreIoBoard()) {
            final IoBoard ioBoard = ModelFacade.getIoBoard();
            if (ioBoard != null && ioBoard.getError() != null) {
                ierror = ioBoard.getError().toString();
            }
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getCurrentPrinter().getInternalState();
        }
        if (request.isAjax()) {
            renderJSON(ModelFacade.isError());
        } else {
            renderArgs.put("isError", ModelFacade.isError());
            renderArgs.put("mstatus", ModelFacade.getState());
            renderArgs.put("merror", ModelFacade.getError());
            renderArgs.put("pstatus", pstatus);
            renderArgs.put("gstatus", gstatus);
            renderArgs.put("gerror", gerror);
            renderArgs.put("ierror", ierror);
            render();
        }
    }

}
