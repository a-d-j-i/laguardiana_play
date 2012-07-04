package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import models.LgUser;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;

/**
 *
 * @author adji
 *
 * A secure controller based on Secure module
 */
public class SecureController extends Controller {

    @Before( unless = { "login", "authenticate", "logout" } )
    static void checkAccess() throws Throwable {
        // Authentication
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        if ( user == null ) {
            flash.put( "url", "GET".equals( request.method ) ? request.url : Play.ctxPath + "/" ); // seems a good default
            login();
        } else {
            if ( !checkPermission( request.action, request.method ) ) {
                Logger.error( "User %s not allowed to access %s %s", user.username, request.action, request.method );
                flash.error( "Not allowed." );
                flash.put( "url", "GET".equals( request.method ) ? request.url : Play.ctxPath + "/" ); // seems a good default
                login();
            }
        }
    }

    public static boolean checkPermission( String resource, String operation ) {
        LgUser user = Cache.get( session.getId() + "-user", LgUser.class );
        if ( user.checkPermission( "ADMIN", "ADMIN" ) ) {
            return true;
        }
        return user.checkPermission( resource, operation );
    }

    public static void login() throws Throwable {
        flash.keep( "url" );
        render();
    }

    public static void authenticate( @Required String username, String password, boolean remember ) throws Throwable {
        // Check tokens

        List<LgUser> users = LgUser.find( "byUsername", username ).fetch();
        LgUser validated = null;
        for ( LgUser user : users ) {
            if ( user.authenticate( password ) ) {
                validated = user;
                break;
            }
        }
        if ( validated == null ) {
            flash.error( "Invalid userid or password." );
        }

        if ( validation.hasErrors() || validated == null ) {
            flash.keep( "url" );
            flash.error( "secure.error" );
            params.flash();
            login();
            return;
        }
        // Mark user as connected
        String expire = "30mn";
        if ( remember ) {
            expire = "30d";
        }
        Cache.set( session.getId() + "-user", validated, expire );
        redirectToOriginalURL();
    }

    public static void logout() throws Throwable {
        Cache.delete( session.getId() + "-user" );
        session.clear();
        flash.success( "secure.logout" );
        login();
    }

    static void redirectToOriginalURL() throws Throwable {
        String url = flash.get( "url" );
        if ( url == null ) {
            url = Play.ctxPath + "/";
        }
        redirect( url );
    }

    public static String md5( String password ) {
        byte[] bytesOfMessage = password.getBytes();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance( "MD5" );
        } catch ( NoSuchAlgorithmException e ) {
            Logger.fatal( e, "System configuration error" );
            return null;
        }
        byte[] thedigest = md.digest( bytesOfMessage );
        String passwordHash = new String( thedigest );
        return passwordHash;
    }
}
