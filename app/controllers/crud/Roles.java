package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgRole;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgRole.class )
public class Roles extends CrudBaseController {
}

