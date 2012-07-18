package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgBill;
import play.mvc.With;

@CRUD.For( LgBill.class )
public class Bills extends CrudBaseController {
}

