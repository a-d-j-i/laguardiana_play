package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgResource;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgResource.class )
public class Resources extends CrudBaseController {
}

