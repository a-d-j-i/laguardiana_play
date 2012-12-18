package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import models.db.LgAclRule;
import models.db.LgRole;
import models.db.LgUser;
import play.Logger;

@Entity
public class User extends LgUser {

    final static public String GUEST_NAME = "guest";

    public User() {
    }

    public static User authenticate(String username, String password) {
        List<User> users;
        if (password != null && !password.isEmpty()) {
            users = User.find("select u from User u where u.username = ? and u.password = ? and "
                    + "( u.endDate is null or u.endDate > CURRENT_TIMESTAMP )", username, password).fetch();
        } else {
            users = User.find("select u from User u where u.username = ? and ( u.password is null or password = '' ) and "
                    + "( u.endDate is null or u.endDate > CURRENT_TIMESTAMP )", username).fetch();
        }
        User validated = null;
        for (User user : users) {
            Logger.debug("Validating user %s", user.username);
            if (user.authenticate(password)) {
                Logger.debug("Validated user %s", user.username);
                validated = user;
                break;
            }
        }
        return validated;
    }

    public boolean isGuest() {
        for (LgRole r : roles) {
            if (r.name.equalsIgnoreCase(GUEST_NAME)) {
                return true;
            }
        }
        return false;
    }

    public void setGuest() {
        LgRole guestRole = LgRole.find("byName", GUEST_NAME).first();
        if (guestRole == null) {
            return;
        }
        // Create automatically the guest user.
        username = "Guest User";
        roles.add(guestRole);
        postLoad();
    }

    class PermsKey {

        String resource;
        String operation;

        public PermsKey(String resource, String operation) {
            this.resource = resource;
            this.operation = operation;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PermsKey other = (PermsKey) obj;
            if ((this.resource == null) ? (other.resource != null) : !this.resource.equals(other.resource)) {
                return false;
            }
            if ((this.operation == null) ? (other.operation != null) : !this.operation.equals(other.operation)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.resource != null ? this.resource.hashCode() : 0);
            hash = 41 * hash + (this.operation != null ? this.operation.hashCode() : 0);
            return hash;
        }
    }
    @Transient
    transient private Map<PermsKey, LgAclRule> perms = new HashMap<PermsKey, LgAclRule>();

    public boolean authenticate(String token) {
        if (password == null || !password.equals(token)) {
            return false;
        }
        if (endDate != null && endDate.before(new Date())) {
            return false;
        }
        return true;
    }

    @PostLoad
    public void postLoad() {
        // TODO: Implement that with a query.
        for (LgRole rol : roles) {
            Logger.info("\trole: %s", rol.toString());
            for (LgAclRule rule : rol.aclRules) {
                PermsKey k = new PermsKey(rule.resource.name, rule.operation);
                perms.put(k, rule);
                Logger.info("\t\t %s", rule.toString());
            }
        }
    }

    public boolean checkPermission(String resource, String operation) {
        PermsKey k = new PermsKey(resource, operation);
        if (!perms.containsKey(k)) {
            return false;
        }
        LgAclRule rule = perms.get(k);
        if (rule == null) {
            return false;
        }
        if (rule.permission.equalsIgnoreCase("ALLOW")) {
            return true;
        } else if (rule.permission.equalsIgnoreCase("DENY")) {
            return false;
        }
        Logger.error("Unauthorized %s %s", resource, operation);
        return false;
    }

    @Override
    public String toString() {
        return username;
    }

    public static String md5(String password) {
        byte[] bytesOfMessage = password.getBytes();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Logger.fatal(e, "System configuration error");
            return null;
        }
        byte[] thedigest = md.digest(bytesOfMessage);
        String passwordHash = new String(thedigest);
        return passwordHash;
    }
}
