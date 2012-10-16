/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import devices.DeviceFactory;
import devices.IoBoard;
import devices.glory.manager.GloryManager;
import models.ModelFacade;
import models.db.LgSystemProperty;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.With;

/**
 * Base class for all the controllers that use the counter to deposit.
 *
 * @author adji
 */
@With({Secure.class})
public class CounterController extends Controller {

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        renderArgs.put("useHardwareKeyboard", isProperty("useHardwareKeyboard"));

        String neededController = ModelFacade.getNeededController();
        if (neededController == null) {
            return;
        }
        String neededAction = ModelFacade.getNeededAction();
        Logger.debug("Needed action : %s  Needed controller : %s", neededAction, neededController);
        if (neededController == null || neededAction == null) {
            return;
        }
        if (neededAction.equalsIgnoreCase("counterError")) {
            neededController = "CounterController";
        }
        if (!request.controller.equalsIgnoreCase(neededController)
                || !request.actionMethod.equalsIgnoreCase(neededAction)) {
            Logger.debug("basicPropertiesAndFixWizard REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            redirect(Router.getFullUrl(neededController + "." + neededAction));
        }
    }

    public static void counterError(Integer cmd) {
        GloryManager.Status gstatus = null;
        String gerror = null;
        IoBoard.Status istatus = null;
        String ierror = null;

        final GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getStatus().getErrorDetail();
        }
        final IoBoard ioBoard = DeviceFactory.getIoBoard();
        if (ioBoard != null) {
            IoBoard.IoBoardStatus s = ioBoard.getStatus();
            istatus = s.status;
            ierror = s.error;
        }
        if (cmd != null) {
            switch (cmd) {
                case 1:
                    ModelFacade.errorReset();
                    break;
                case 2:
                    ModelFacade.storingErrorReset();
                    break;
            }
        }
        renderArgs.put("mstatus", ModelFacade.getState());
        renderArgs.put("merror", ModelFacade.getError());
        renderArgs.put("gstatus", gstatus);
        renderArgs.put("gerror", gerror);
        renderArgs.put("istatus", istatus);
        renderArgs.put("ierror", ierror);

        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = gstatus.name();
            if (gerror != null) {
                o[1] = gerror.toString();
            } else {
                o[1] = "";
            }
            renderJSON(o);
        } else {
            renderArgs.put("isError", ModelFacade.isError());
            render();
        }
    }

    @Util
    static public String getProperty(LgSystemProperty.Types type) {
        return LgSystemProperty.getProperty(type);
    }

    @Util
    static public Integer getIntProperty(LgSystemProperty.Types type) {
        try {
            return Integer.parseInt(LgSystemProperty.getProperty(type));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Util
    static public Boolean isProperty(String name) {
        return LgSystemProperty.isProperty(name);
    }
}
