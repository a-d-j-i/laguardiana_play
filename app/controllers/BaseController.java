package controllers;

import java.util.List;
import models.LgUser;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

/**
 *
 * @author adji
 *
 * Base controller for all lg controllers, uses security and auth.
 */
@With( SecureController.class )
public class BaseController extends Controller {
 
}
