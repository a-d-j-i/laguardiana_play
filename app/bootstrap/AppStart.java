package bootstrap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import models.ModelFacade;
import models.db.LgAclRule;
import models.db.LgResource;
import models.db.LgRole;
import models.db.LgSystemProperty;
import models.db.LgUser;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Util;
import play.test.Fixtures;

/**
 *
 * @author adji
 */
@OnApplicationStart
public class AppStart extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("AppVersion : %s", ModelFacade.getAppVersion());
        Logger.debug("onApplicationStart populate roles");
        populateRoles();
        Logger.debug("onApplicationStart open all devices");
        ModelFacade.start();
        Logger.debug("onApplicationStart open all devices DONE");
    }

    @Util
    private void populateRoles() {
        // Load roles.
        if (LgSystemProperty.find("select l from LgSystemProperty l where name = 'db_initialized'").fetch().isEmpty()) {
            Fixtures.deleteDatabase();
            LgSystemProperty l = new LgSystemProperty();
            l.name = "db_initialized";
            l.value = "done";
            l.save();

            Logger.info("loading user-data.yml as no users were found!");
            Fixtures.loadModels("user-data.yml");
            // Menus
            LgRole mainMenu = loadRolsResourcesAndAclsMenu("mainMenu");
            LgRole otherMenu = loadRolsResourcesAndAclsMenu("otherMenu");
            LgRole hardwareMenu = loadRolsResourcesAndAclsMenu("hardwareMenu");
            LgRole printTemplateMenu = loadRolsResourcesAndAclsMenu("printTemplateMenu");
            LgRole accountingMenu = loadRolsResourcesAndAclsMenu("accountingMenu");
            LgRole reportMenu = loadRolsResourcesAndAclsMenu("reportMenu");

            // Controllers
            LgRole bill = loadRolsResourcesAndAcls(controllers.BillDepositController.class);
            LgRole count = loadRolsResourcesAndAcls(controllers.CountController.class);
            LgRole envelope = loadRolsResourcesAndAcls(controllers.EnvelopeDepositController.class);
            LgRole filter = loadRolsResourcesAndAcls(controllers.FilterController.class);
            LgRole glory = loadRolsResourcesAndAcls(controllers.GloryDE50Controller.class);
            //LgRole manager = loadRolsResourcesAndAcls(controllers.DeviceCounterClassController.class);
            LgRole ioboard = loadRolsResourcesAndAcls(controllers.IoBoardController.class);
            LgRole counter = loadRolsResourcesAndAcls(controllers.ErrorController.class);
            LgRole report = loadRolsResourcesAndAcls(controllers.ReportController.class);
            LgRole reportDesposit = loadRolsResourcesAndAcls(controllers.ReportDepositController.class);
            LgRole reportZ = loadRolsResourcesAndAcls(controllers.ReportZController.class);
            LgRole reportBag = loadRolsResourcesAndAcls(controllers.ReportBagController.class);
            LgRole printer = loadRolsResourcesAndAcls(controllers.PrinterController.class);

            // Reset and Storing reset
            LgRole reset = new LgRole();
            reset.name = "Reset";
            reset.save();
            createAcl(reset, "Application.reset");
            LgRole storingReset = new LgRole();
            storingReset.name = "StoringReset";
            storingReset.save();
            createAcl(storingReset, "Application.storingReset");

            // Add application envelope and bill to demo.
            LgUser guest = LgUser.find("select u from LgUser u where username = 'guest'").first();
            /*guest.roles.add(appMin);
             appMin.users.add(guest);*/
            guest.roles.add(counter);
            counter.users.add(guest);
            guest.save();

            LgUser demo = LgUser.find("select u from LgUser u where username = 'demo'").first();
            demo.roles.add(mainMenu);
            mainMenu.users.add(demo);

            demo.roles.add(counter);
            counter.users.add(demo);
            demo.roles.add(bill);
            bill.users.add(demo);
            demo.roles.add(envelope);
            envelope.users.add(demo);
            demo.save();

            LgUser user = LgUser.find("select u from LgUser u where username = 'user'").first();
            user.roles.add(mainMenu);
            mainMenu.users.add(user);

            user.roles.add(counter);
            counter.users.add(user);
            user.roles.add(bill);
            bill.users.add(user);
            user.save();

            LgUser supervisor = LgUser.find("select u from LgUser u where username = 'supervisor'").first();
            supervisor.roles.add(mainMenu);
            mainMenu.users.add(supervisor);
            supervisor.roles.add(otherMenu);
            otherMenu.users.add(supervisor);
            supervisor.roles.add(accountingMenu);
            accountingMenu.users.add(supervisor);

            supervisor.roles.add(counter);
            counter.users.add(supervisor);
            supervisor.roles.add(bill);
            bill.users.add(supervisor);
            supervisor.roles.add(envelope);
            envelope.users.add(supervisor);
            supervisor.roles.add(report);
            report.users.add(supervisor);
            supervisor.roles.add(reportBag);
            reportBag.users.add(supervisor);
            supervisor.roles.add(reportZ);
            reportZ.users.add(supervisor);
            supervisor.roles.add(reportDesposit);
            reportDesposit.users.add(supervisor);
            supervisor.save();

            Logger.info("loading lov-data.yml");
            Fixtures.loadModels("lov-data.yml");
            Logger.info("loading sys-props-data.yml");
            Fixtures.loadModels("sys-props-data.yml");
            Logger.info("loading dev-data.yml");
            Fixtures.loadModels("dev-data.yml");
        }
    }

    @Util
    static LgRole loadRolsResourcesAndAcls(Class c) {
        String roleName = c.getSimpleName();
        LgRole lr = LgRole.find("select l from LgRole l where name = ?", roleName).first();
        if (lr == null) {
            lr = new LgRole();
            lr.name = roleName;
            lr.save();
        }
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isAnnotationPresent(Util.class)) {
                String name = c.getSimpleName() + "." + m.getName();
                createAcl(lr, name);
            }
        }
        return lr;
    }

    @Util
    static LgRole loadRolsResourcesAndAclsMenu(String menuName) {
        String roleName = "Menu_" + menuName;
        LgRole lr = LgRole.find("select l from LgRole l where name = ?", roleName).first();
        if (lr == null) {
            lr = new LgRole();
            lr.name = roleName;
            lr.save();
        }
        String name = controllers.MenuController.class.getSimpleName() + "." + menuName;
        createAcl(lr, name);
        return lr;
    }

    @Util
    static void createAcl(LgRole lr, String name) {
        LgResource l = LgResource.find("select l from LgResource l where name = ?", name).first();
        if (l == null) {
            l = new LgResource();
            l.name = name;
        }
        l.save();
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
