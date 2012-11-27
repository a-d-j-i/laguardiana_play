package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Configuration;
import models.User;
import models.db.LgSystemProperty;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Header;
import play.mvc.Router;

/**
 *
 * @author adji
 *
 * A secure controller based on Secure module
 */
public class Secure extends Controller {

    @Before(unless = {"login", "authenticate", "logout"})
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
        if (Configuration.isAllAlowed()) {
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

    public static void login() {
        String url = flash.get("lastUrl");
        if (url == null) {
            url = Play.ctxPath + "/";
        }
        renderArgs.put("lastUrl", url);
        // TODO: Put in some other place.
        Logger.error("URL %s", url);


        flash.keep("url");
        flash.keep("lastUrl");
        render();
    }

    public static void authenticate(@Required String username, @Required String password, boolean remember) throws Throwable {
        // The keyboard only types uppercase.
        if (password != null) {
            password = password.toLowerCase();
        }
        if (username != null) {
            username = username.toLowerCase();
        }

        if (Validation.hasErrors()) {
            Logger.info("validation hasErrors!!!");
            for (Error error : validation.errors()) {
                Logger.error("%s    %s", error.message(), error.getKey());
            }
            flash.keep("url");
            flash.error("secure.invalid_field");
            params.flash();
            if (request.isAjax()) {
                String[] d = {"error", Messages.get("secure.invalid_field")};
                renderJSON(d);
                return;
            } else {
                login();
                return;
            }
        }

        Logger.info("received user: %s password: %s", username, password);

        User user = User.authenticate(username, password);
        if (user == null) {
            Logger.error("no such user!");
            flash.keep("url");
            flash.error("secure.invalid_user_password");
            params.flash();
            if (request.isAjax()) {
                String[] d = {"error", Messages.get("secure.invalid_user_password")};
                renderJSON(d);
                return;
            } else {
                login();
                return;
            }
        }

        // Mark user as connected
        String expire = "30mn";
        if (remember) {
            expire = "30d";
        }
        Cache.set(session.getId() + "-user", user, expire);
        if (request.isAjax()) {
            String[] d = {"success", getOriginalUrl()};
            renderJSON(d);
        } else {
            String a = flash.get("authenticated");
            flash.put("authenticated", "authenticated");
            if (a != null) {
                redirect("/");
            } else {
                Map<String, String> m = Router.route("GET", getOriginalUrl());
                if (checkPermission(m.get("action"), "GET")) {
                    redirect(getOriginalUrl());
                } else {
                    redirect("/");
                }
            }
        }
    }

    public static void logout(String toUrl) throws Throwable {
        Cache.delete(session.getId() + "-user");
        session.clear();
        //flash.success( "secure.logout" );
        if (toUrl == null || toUrl.isEmpty()) {
            redirect(getOriginalUrl());
        } else {
            String url = Router.reverse(toUrl, new HashMap()).url;
            redirect(url);
        }
    }

    static String getOriginalUrl() throws Throwable {
        String url = flash.get("url");
        if (url == null) {
            url = Play.ctxPath + "/";
        }
        return url;
    }

    // Is it ok to be public?? 
    public static User getCurrentUser() {
        User user = null;
        if (session != null) {
            user = Cache.get(session.getId() + "-user", User.class);
        }
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
                if (session != null) {
                    Cache.set(session.getId() + "-user", user);
                }
            } else {
                login();
                return null;
            }
        }
        return user;
    }
}
