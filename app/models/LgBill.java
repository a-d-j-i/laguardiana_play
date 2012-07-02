package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bill", schema = "public" )
public class LgBill extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bill_id", unique = true, nullable = false )
    int billId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id", nullable = false )
    LgDeposit lgDeposit;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "batch_id", nullable = false )
    LgBatch lgBatch;
    @Column( name = "slot_id", nullable = false )
    int slotId;
    @Column( name = "quantity", nullable = false )
    int quantity;
    @Column( name = "denomination", nullable = false )
    int denomination;
    @Column( name = "unit_lob", nullable = false )
    int unitLob;
}
