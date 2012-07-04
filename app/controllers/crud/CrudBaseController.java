/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.crud;

import controllers.BaseController;
import controllers.CRUD;
import play.Logger;
import play.mvc.With;

/**
 *
 * @author adji
 */
@With( BaseController.class )
public class CrudBaseController extends CRUD {

    public static void index() {
        render( "CRUD/index.html" );
    }
}
