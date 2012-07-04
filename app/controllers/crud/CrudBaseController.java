/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.crud;

import controllers.CRUD;
import controllers.Check;
import controllers.Secure;
import play.mvc.With;

/**
 *
 * @author adji
 */
@With( Secure.class )
@Check( "value" )
public class CrudBaseController extends CRUD {
}
