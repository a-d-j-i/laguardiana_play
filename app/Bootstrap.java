
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import models.db.LgAclRule;
import models.db.LgBag;
import models.db.LgLov;
import models.db.LgResource;
import models.db.LgRole;
import models.db.LgSystemProperty;
import models.db.LgUser;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Util;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        // Load roles.
        Fixtures.deleteDatabase();

        Logger.info("loading user-data.yml as no users were found!");
        Fixtures.loadModels("user-data.yml");
        LgRole app = loadRolsResourcesAndAcls(controllers.Application.class);
        LgRole appMin = loadRolsResourcesAndAclsMin(controllers.Application.class, true);
        LgRole bill = loadRolsResourcesAndAcls(controllers.BillDepositController.class);
        LgRole count = loadRolsResourcesAndAcls(controllers.CountController.class);
        LgRole envelope = loadRolsResourcesAndAcls(controllers.EnvelopeDepositController.class);
        LgRole filter = loadRolsResourcesAndAcls(controllers.FilterController.class);
        LgRole glory = loadRolsResourcesAndAcls(controllers.GloryController.class);
        LgRole manager = loadRolsResourcesAndAcls(controllers.GloryManagerController.class);
        LgRole ioboard = loadRolsResourcesAndAcls(controllers.IoBoardController.class);

        // Add application envelope and bill to demo.
/*        LgUser guest = LgUser.find("select u from LgUser u where username = 'guest'").first();
         guest.roles.add(app);
         app.users.add(guest);
         guest.save();*/

        LgUser demo = LgUser.find("select u from LgUser u where username = 'demo'").first();
        demo.roles.add(appMin);
        appMin.users.add(demo);
        demo.roles.add(bill);
        bill.users.add(demo);
        demo.roles.add(envelope);
        envelope.users.add(demo);
        demo.save();



        if (LgLov.count() == 0) {
            Logger.info("loading lov-data.yml as no data were found!");
            Fixtures.loadModels("lov-data.yml");
        }
        if (LgSystemProperty.count() == 0) {
            Logger.info("loading sys-props-data.yml as no data were found!");
            Fixtures.loadModels("sys-props-data.yml");
        }
        Logger.info(String.format("Glory port : %s", Play.configuration.getProperty("glory.port")));
        if (Play.mode.isDev()) {
            if (LgBag.count() == 0) {
                Fixtures.loadModels("dev-data.yml");
            }
        }
        // Start glory Manager
        //CounterFactory.getManager( Play.configuration.getProperty( "glory.port" ) );
    }

    @Util
    static LgRole loadRolsResourcesAndAcls(Class c) {
        return loadRolsResourcesAndAclsMin(c, false);
    }

    // Min is a hack for application controller.
    @Util
    static LgRole loadRolsResourcesAndAclsMin(Class c, boolean min) {
        String roleName = c.getSimpleName();
        if (min) {
            roleName += "MIN";
        }
        LgRole lr = LgRole.find("select l from LgRole l where name = ?", roleName).first();
        if (lr == null) {
            lr = new LgRole();
            lr.name = roleName;
            lr.save();
        }
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isAnnotationPresent(Util.class)) {
                String name = c.getSimpleName() + m.getName().substring(0, 1).toUpperCase() + m.getName().substring(1);
                LgResource l = LgResource.find("select l from LgResource l where name = ?", name).first();
                if (l == null) {
                    l = new LgResource();
                    l.name = c.getSimpleName() + "." + m.getName();
                }
                l.save();
                if (min && c.equals(controllers.Application.class)) {
                    if (!m.getName().equals("index")
                            && !m.getName().equals("mainMenu")) {
                        continue;
                    }
                }
                // Get
                LgAclRule lacl = LgAclRule.find("select l from LgAclRule l "
                        + "where operation = 'GET' and permission = 'ALLOW' "
                        + " and resource = ? and role = ? ", l, lr).first();
                if (lacl == null) {
                    lacl = new LgAclRule();
                    lacl.operation = "GET";
                    lacl.permission = "ALLOW";
                    lacl.resource = l;
                    lacl.role = lr;
                }
                lacl.save();
                // Post
                lacl = LgAclRule.find("select l from LgAclRule l "
                        + "where operation = 'POST' and permission = 'ALLOW' "
                        + " and resource = ? and role = ? ", l, lr).first();
                if (lacl == null) {
                    lacl = new LgAclRule();
                    lacl.operation = "POST";
                    lacl.permission = "ALLOW";
                    lacl.resource = l;
                    lacl.role = lr;
                }
                lacl.save();
            }
        }
        return lr;
    }
}
