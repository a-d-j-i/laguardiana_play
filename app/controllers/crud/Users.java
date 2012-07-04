package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgUser;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgUser.class )
public class Users extends CrudBaseController {
}

