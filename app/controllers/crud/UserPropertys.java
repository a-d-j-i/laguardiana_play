package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgUserProperty;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgUserProperty.class )
public class UserPropertys extends CrudBaseController {
}

