package models;

import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_bill", schema = "public" )
public class LgBill extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "bill_id", unique = true, nullable = false )
    @GeneratedValue
    public int billId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "deposit_id", nullable = false )
    public LgDeposit deposit;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "batch_id", nullable = false )
    public LgBatch batch;
    @Column( name = "slot_id", nullable = false )
    public int slotId;
    @Column( name = "quantity", nullable = false )
    public int quantity;
    @Column( name = "denomination", nullable = false )
    public int denomination;
    @Column( name = "unit_lob", nullable = false )
    public int unitLob;
    
    public LgBill(LgBatch batch, int slotId, int quantity, int denomination, int unitLob) {
        this.batch = batch;
        this.slotId = slotId;
        this.quantity = quantity;
        this.denomination = denomination;
        this.unitLob = unitLob;
    }
}
