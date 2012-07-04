package controllers.crud;

import controllers.CRUD;
import models.LgUser;
import play.mvc.With;

@CRUD.For( LgUser.class )
public class Users extends CrudBaseController {
}

