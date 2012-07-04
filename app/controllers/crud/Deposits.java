package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgDeposit;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgDeposit.class )
public class Deposits extends CrudBaseController {
}

