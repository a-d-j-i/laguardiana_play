package jobs;

import java.util.Date;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.*;

/**
 * Fire at 12pm (noon) every day *
 */
@On("0 0 12 * * ?")
public class PurgeDatabase extends Job {

    static final String PURGE_INTERVAL = "1 month";
    static final String PROCESSED_PURGE_INTERVAL = "1 year";

    @Override
    public void doJob() {
        Logger.debug("Start database purge at: " + (new Date()).toString());
        play.db.DB.execute("delete from lg_event where creation_date < ( now() -  interval '" + PURGE_INTERVAL + "');");
        play.db.DB.execute(""
                + " create or replace view lg_processed_to_delete_view"
                + " as select log_type, log_source_id, count(distinct log_source_id) as cnt"
                + " from lg_external_app_log"
                + " where success_date < ( now() -  interval '" + PURGE_INTERVAL + "')"
                + " group by log_type, log_source_id"
                + " having count(distinct log_source_id) = (select count(*) from lg_external_app)");
        play.db.DB.execute(""
                + " delete from lg_bill"
                + " where deposit_id in (select log_source_id as deposit_id from"
                + "                     lg_processed_to_delete_view where log_type ='DEPOSIT'"
                + " )");
        play.db.DB.execute(""
                + " delete from lg_envelope_content"
                + " where envelope_id in "
                + "     (select envelope_id from lg_envelope where deposit_id in"
                + "         (select log_source_id as deposit_id from"
                + "                     lg_processed_to_delete_view where log_type ='DEPOSIT'"
                + " ))");
        play.db.DB.execute(""
                + " delete from lg_envelope"
                + " where deposit_id in (select log_source_id as deposit_id from"
                + "                     lg_processed_to_delete_view where log_type ='DEPOSIT'"
                + " )");
        play.db.DB.execute(""
                + " delete from lg_deposit"
                + " where deposit_id in (select log_source_id as deposit_id from"
                + "                     lg_processed_to_delete_view where log_type ='DEPOSIT')"
        );
        play.db.DB.execute(""
                + " delete from lg_bag "
                + " where bag_id in (select log_source_id as bag_id from"
                + "                 lg_processed_to_delete_view where log_type ='BAG')"
        );
        play.db.DB.execute(""
                + " delete from lg_batch a"
                + " where not exists (select b.bill_id from lg_bill b where a.batch_id = b.batch_id)"
                + " and creation_date < ( now() -  interval '" + PURGE_INTERVAL + "')"
        );
        play.db.DB.execute(""
                + " delete from lg_z a"
                + " where not exists (select b.z_id from lg_deposit b where a.z_id = b.z_id)"
                + " and creation_date < ( now() -  interval '" + PURGE_INTERVAL + "')"
        );
        // finally lg_external_app_log, but keep a year at least.
        play.db.DB.execute(""
                + " delete from lg_external_app_log where success_date < ( now() -  interval '" + PROCESSED_PURGE_INTERVAL + "')"
        );
        JPA.em().flush();
        JPA.em().getTransaction().commit();

        play.db.DB.execute("VACUUM");
        Logger.debug("End database purge at: " + (new Date()).toString());
    }

}
