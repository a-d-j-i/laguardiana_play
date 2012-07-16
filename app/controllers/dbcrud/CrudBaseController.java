/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.dbcrud;

import controllers.CRUD;
import controllers.Secure;
import play.mvc.With;

/**
 *
 * @author adji
 */
@With( Secure.class )
public class CrudBaseController extends CRUD {

    public static void index() {
        render( "CRUD/index.html" );
    }
}
