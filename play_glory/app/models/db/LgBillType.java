package models.db;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import models.BillValue;
import models.lov.Currency;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_bill_type", schema = "public")
public class LgBillType extends GenericModel implements java.io.Serializable {

    @Id
    @Column(name = "bill_type_id", unique = true, nullable = false)
    @GeneratedValue(generator = "LgBillTypeGenerator")
    @SequenceGenerator(name = "LgBillTypeGenerator", sequenceName = "lg_bill_type_sequence")
    public Integer billTypeId;
    @Column(name = "denomination", nullable = false)
    public Long denomination;
    @Column(name = "unit_lov", nullable = false)
    public Integer unitLov;
    // TODO: Put in some place in glory configuration.
    @Column(name = "slot", nullable = false)
    public Integer slot;
    @Column(name = "currency", nullable = false)
    public Integer currency;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;

    LgBillType(long denomination, int unitLov) {
        this.denomination = denomination;
        this.unitLov = unitLov;
    }

    public static LgBillType find(int denomination, int unitLov) {
        return LgBillType.find("select l from LgBillType l where denomination = ? and unitLov = ? and ( end_date is null or end_date > current_timestamp())",
                denomination, unitLov).first();
    }

    public static List< LgBillType> find(int currency) {
        return LgBillType.find("select l from LgBillType l where currency = ? and ( end_date is null or end_date > current_timestamp()) order by denomination desc", currency).fetch();
    }

    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    @Override
    public String toString() {
        Long d = denomination;
        return (d.toString() + " " + Currency.findByNumericId(unitLov));
    }
    transient private Currency unitLovObj = null;

    public Currency getCurrency() {
        if (unitLovObj == null) {
            unitLovObj = Currency.findByNumericId(unitLov);
        }
        return unitLovObj;
    }

    public BillValue getValue() {
        return new BillValue(unitLov, denomination);
    }
}
