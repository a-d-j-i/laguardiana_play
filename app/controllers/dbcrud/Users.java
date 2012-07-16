package controllers.dbcrud;

import controllers.CRUD;
import models.LgUser;
import play.mvc.With;

@CRUD.For( LgUser.class )
public class Users extends CrudBaseController {
}

