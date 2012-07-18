package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgEvent;
import play.mvc.With;

@CRUD.For( LgEvent.class )
public class Events extends CrudBaseController {
}

