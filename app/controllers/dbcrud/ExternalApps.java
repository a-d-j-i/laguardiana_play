package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgExternalApp;
import play.mvc.With;

@CRUD.For( LgExternalApp.class )
public class ExternalApps extends CrudBaseController {
}

