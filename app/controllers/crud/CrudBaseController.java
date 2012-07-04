/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.crud;

import controllers.CRUD;
import controllers.SecureController;
import play.mvc.With;

/**
 *
 * @author adji
 */
@With( SecureController.class )
public class CrudBaseController extends CRUD {

    public static void index() {
        render( "CRUD/index.html" );
    }
}
