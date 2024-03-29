package models.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import models.Configuration;
import play.Logger;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_user", schema = "public")
public class LgUser extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgUserGenerator")
    @SequenceGenerator(name = "LgUserGenerator", sequenceName = "lg_user_sequence")
    public Integer userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_app_id", nullable = false)
    public LgExternalApp externalApp;
    @Required
    @MinSize(8)
    @Column(name = "username", nullable = false, length = 128)
    public String username;
    @Column(name = "password", nullable = false, length = 128)
    protected String password;
    @Column(name = "gecos", nullable = true, length = 128)
    public String gecos;
    @Column(name = "external_id", nullable = false, length = 32)
    public String externalId;
    @Column(name = "locked", nullable = false, length = 1)
    public byte locked;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    public Set<LgEvent> events = new HashSet<LgEvent>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    public Set<LgDeposit> deposits = new HashSet<LgDeposit>(0);
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lg_user_role", schema = "public", joinColumns = {
        @JoinColumn(name = "user_id", nullable = false, updatable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "role_id", nullable = false, updatable = false)})
    public Set<LgRole> roles = new HashSet<LgRole>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    public Set<LgUserProperty> userProperties = new HashSet<LgUserProperty>(0);

    public void setPassword(String password) {
        this.password = password;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        password = null;
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    @Override
    public String toString() {
        return "LgUser : " + "userId=" + userId + ", username=" + username + ", externalId=" + externalId + ", locked=" + locked + ", creationDate=" + creationDate + ", endDate=" + endDate + '}';
    }
    final static public String GUEST_NAME = "guest";

    public static LgUser authenticate(String username, String password) {
        List<LgUser> users = LgUser.find("select u from LgUser u where u.username = ? and "
                + "( u.endDate is null or u.endDate > current_timestamp() )", username).fetch();
        LgUser validated = null;
        for (LgUser user : users) {
            Logger.debug("Validating user %s", user.username);
            if (user.authenticate(password)) {
                Logger.debug("Validated user %s", user.username);
                validated = user;
                break;
            }
        }
        return validated;
    }

    public boolean authenticate(String token) {
        if (endDate != null && endDate.before(new Date())) {
            return false;
        }
        if (Configuration.isCrapAuth()) {
            String crapAuthVariableId = Configuration.getCrapAuthId();
            String crapAuthConstantId = Configuration.getCrapAuthConstantId();
            if (crapAuthVariableId != null && crapAuthConstantId != null) {
                try {
                    String inv = new StringBuilder(crapAuthVariableId).reverse().toString();
                    Integer s = Integer.parseInt(crapAuthConstantId) + Integer.parseInt(inv);
                    char[] ss = s.toString().toCharArray();
                    if (ss != null && ss.length >= 6) {
                        if (LgUserProperty.isProperty(LgUserProperty.Types.CRAP_AUTH)) {
                            String key = "" + ss[0] + ss[2] + ss[4] + ss[1] + ss[3] + ss[5];
                            Logger.debug("crapAuth key %s", key);
                            if (key.equals(token)) {
                                Configuration.initCrapId();
                                return true;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    Logger.error("crapAuth invalid numbers constant %s variable %s", crapAuthConstantId, crapAuthVariableId);
                }
            } else {
                Logger.error("crapAuth invalid numbers constant %s variable %s", crapAuthConstantId, crapAuthVariableId);
            }
        }
        if (Configuration.dontAskForPassword()) {
            if (password == null || password.trim().isEmpty()) {
                return true;
            }
        } else {
            if (password == null || password.trim().isEmpty()) {
                return false;
            }
        }
        if (!password.equals(token) || password.equalsIgnoreCase("X") || password.equalsIgnoreCase("!")) {
            return false;
        }
        return true;
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

    public boolean isAdmin() {
        return checkPermission("ADMIN", "ADMIN");
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

    @PostLoad
    public void postLoad() {
        // TODO: Implement that with a query.
        for (LgRole rol : roles) {
            //Logger.info("\trole: %s", rol.toString());
            for (LgAclRule rule : rol.aclRules) {
                PermsKey k = new PermsKey(rule.resource.name, rule.operation);
                perms.put(k, rule);
                //Logger.info("\t\t %s", rule.toString());
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.userId != null ? this.userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LgUser other = (LgUser) obj;
        return (this.userId == other.userId);
    }

}
