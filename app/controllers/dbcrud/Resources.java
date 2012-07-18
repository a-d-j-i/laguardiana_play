package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgResource;
import play.mvc.With;

@CRUD.For( LgResource.class )
public class Resources extends CrudBaseController {
}

