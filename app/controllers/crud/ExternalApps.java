package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgExternalApp;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgExternalApp.class )
public class ExternalApps extends CrudBaseController {
}

