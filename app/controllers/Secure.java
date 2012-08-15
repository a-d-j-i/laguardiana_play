package controllers;

import java.util.List;
import models.User;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Header;

/**
 *
 * @author adji
 *
 * A secure controller based on Secure module
 */
public class Secure extends Controller {

    @Before( unless = {"login", "authenticate", "logout"})
    static void checkAccess() throws Throwable {

        if ("GET".equals(request.method)) {
            Header referer = request.headers.get("referer");
            flash.put("url", request.url); // seems a good default
            if (referer != null) {
                flash.put("lastUrl", referer.value()); // seems a good default
            } else {
                flash.put("lastUrl", Play.ctxPath + "/"); // seems a good default
            }
        } else {
            flash.put("url", Play.ctxPath + "/"); // seems a good default
            flash.put("lastUrl", Play.ctxPath + "/"); // seems a good default
        }

        // Authentication
        User user = getCurrentUser();
        if (user == null) {
            login();
            return;
        }
        if (!checkPermission(request.action, request.method)) {
            Logger.error("User %s not allowed to access %s %s", user.username, request.action, request.method);
            flash.error("secure.not_allowed");
            login();
        }
        renderArgs.put("user", user);
    }

    public static boolean checkPermission(String resource, String operation) {
        if (Play.mode.isDev()) {
            Logger.info("IN DEV MODE ALL ALLOWED!!!");
            return true;
        }
        User user = Cache.get(session.getId() + "-user", User.class);
        if (user == null) {
            Logger.error("Invalid user");
            return false;
        }
        if (user.checkPermission("ADMIN", "ADMIN")) {
            return true;
        }
        return user.checkPermission(resource, operation);
    }

    public static void login() throws Throwable {
        String url = flash.get("lastUrl");
        if (url == null) {
            url = Play.ctxPath + "/";
        }
        renderArgs.put("lastUrl", url);
        Logger.error("URL %s", url);


        flash.keep("url");
        flash.keep("lastUrl");
        render();
    }

    public static void authenticate(@Required String username, String password, boolean remember, String cancel) throws Throwable {
        // Check tokens
        Logger.error(cancel);

        if (Validation.hasErrors()) {
            flash.keep("url");
            flash.error("secure.invalid_field");
            params.flash();
            login();
            return;
        }

        User user = User.authenticate(username, password);
        if (user == null) {
            flash.keep("url");
            flash.error("secure.invalid_user_password");
            params.flash();
            login();
            return;
        }

        // Mark user as connected
        String expire = "30mn";
        if (remember) {
            expire = "30d";
        }
        Cache.set(session.getId() + "-user", user, expire);
        redirectToOriginalURL();
    }

    public static void logout() throws Throwable {
        Cache.delete(session.getId() + "-user");
        session.clear();
        //flash.success( "secure.logout" );
        redirectToOriginalURL();
    }

    static void redirectToOriginalURL() throws Throwable {
        String url = flash.get("url");
        if (url == null) {
            url = Play.ctxPath + "/";
        }
        redirect(url);
    }

    static User getCurrentUser() throws Throwable {
        User user = Cache.get(session.getId() + "-user", User.class);
        if (user == null) {
            List<User> q = User.find("byUserName", User.GUEST_NAME).fetch();
            if (q.size() > 1) {
                Logger.info("There are too many guest users, taking the first");
            } else if (q.size() == 0) {
                Logger.error("Create the user guest in the db");
                user = new User();
            }
            if (user == null) {
                user = q.get(0);
            }
            user.setGuest();

            if (user.isGuest()) {
                Cache.set(session.getId() + "-user", user);
            } else {
                login();
                return null;
            }
        }
        return user;
    }
}
