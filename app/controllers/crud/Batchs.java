package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgBatch;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgBatch.class )
public class Batchs extends CrudBaseController {
}

