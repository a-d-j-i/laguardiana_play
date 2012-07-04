package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgEvent;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgEvent.class )
public class Events extends CrudBaseController {
}

