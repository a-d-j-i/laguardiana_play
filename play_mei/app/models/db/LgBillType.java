package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public Integer denomination;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency", nullable = false)
    public Currency currency;

//    @Column(name = "currency", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, length = 13)
    public Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "billType")
    public Set<LgDeviceSlot> slots = new HashSet<LgDeviceSlot>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "billType")
    public Set<LgBill> bills = new HashSet<LgBill>(0);

    LgBillType(int denomination, Currency currency) {
        this.denomination = denomination;
        this.currency = currency;
    }

    public static LgBillType find(int denomination, int unitLov) {
        return LgBillType.find("select l from LgBillType l where denomination = ? and unitLov = ? and ( end_date is null or end_date > current_timestamp())",
                denomination, unitLov).first();
    }

    public static List< LgBillType> find(Currency currency) {
        return LgBillType.find("select l from LgBillType l where currency = ? and ( end_date is null or end_date > current_timestamp()) order by denomination desc", currency).fetch();
    }

    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    @Override
    public String toString() {
        Integer d = denomination;
        return (billTypeId + " == " + d.toString() + " " + currency.toString());
    }

    public BillValue getValue() {
        return new BillValue(currency, denomination);
    }

}
