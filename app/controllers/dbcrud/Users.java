package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgUser;
import play.mvc.With;

@CRUD.For( LgUser.class )
public class Users extends CrudBaseController {
}

