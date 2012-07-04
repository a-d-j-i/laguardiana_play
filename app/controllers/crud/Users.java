package controllers.crud;
 
import play.*;
import play.mvc.*;
import models.*;
import controllers.*;

import models.LgUser;
import play.mvc.With;

 
@With(Secure.class)
@CRUD.For( LgUser.class )
public class Users extends CrudBaseController {
}

