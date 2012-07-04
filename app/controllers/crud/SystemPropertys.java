package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgSystemProperty;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgSystemProperty.class )
public class SystemPropertys extends CrudBaseController {
}

