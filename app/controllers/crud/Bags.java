package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgBag;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgBag.class )
public class Bags extends CrudBaseController {
}

