package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import models.lov.MoneyUnit;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bill_type", schema = "public")
public class LgBillType extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bill_type_id", unique = true, nullable = false)
    @GeneratedValue
    public int billTypeId;
    @Column( name = "denomination", nullable = false)
    public int denomination;
    @Column( name = "unit_lov", nullable = false)
    public int unitLov;
    // TODO: Put in some place in glory configuration.
    @Column( name = "slot", nullable = false)
    public int slot;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "end_date", nullable = true, length = 13)
    public Date endDate;

    LgBillType(int denomination, int unitLov) {
        this.denomination = denomination;
        this.unitLov = unitLov;
    }

    static LgBillType find(int denomination, int unitLov) {
        return LgBillType.find("select l from LgBillType l where denomination = ? and unitLov = ?",
                denomination, unitLov).first();
    }

    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    @Override
    public String toString() {
        Integer d = denomination;
        return (d.toString() + " " + MoneyUnit.findByNumericId(unitLov));
    }
}
