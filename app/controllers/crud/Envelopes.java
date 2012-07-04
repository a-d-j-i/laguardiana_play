package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgEnvelope;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgEnvelope.class )
public class Envelopes extends CrudBaseController {
}

