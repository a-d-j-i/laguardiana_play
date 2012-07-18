package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgExternalAppLog;
import play.mvc.With;

@CRUD.For( LgExternalAppLog.class )
public class ExternalAppLogs extends CrudBaseController {
}

