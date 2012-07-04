package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.LgAclRule;
import play.mvc.With;

@With(Secure.class)
@CRUD.For( LgAclRule.class )
public class AclRules extends CrudBaseController {
}

