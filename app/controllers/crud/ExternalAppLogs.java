package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgExternalAppLog;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgExternalAppLog.class )
public class ExternalAppLogs extends CrudBaseController {
}

