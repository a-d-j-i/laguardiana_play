package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgBill;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgBill.class )
public class Bills extends CrudBaseController {
}

