package controllers;

import play.mvc.Controller;
import play.mvc.With;

/**
 *
 * @author adji
 *
 * Base controller for all lg controllers, uses security and auth.
 */
@With( Secure.class )
public class BaseController extends Controller {
 
}
