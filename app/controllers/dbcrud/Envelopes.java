package controllers.dbcrud;

import controllers.CRUD;
import models.db.LgEnvelope;
import play.mvc.With;

@CRUD.For( LgEnvelope.class )
public class Envelopes extends CrudBaseController {
}

