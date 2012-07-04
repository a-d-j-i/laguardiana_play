package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgLov;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgLov.class )
public class Lovs extends CrudBaseController {
}

