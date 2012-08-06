package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import models.lov.MoneyUnit;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_batch", schema = "public" )
public class LgBatch extends GenericModel implements java.io.Serializable {

    @Id
    @Column( name = "batch_id", unique = true, nullable = false )
    @GeneratedValue
    public int batchId;
    @Temporal( TemporalType.DATE )
    @Column( name = "creation_date", nullable = false, length = 13 )
    public Date creationDate;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "batch" )
    public Set<LgBill> bills = new HashSet<LgBill>( 0 );

    public LgBatch() {
        this.creationDate = new Date();
    };
    
    public void addBill( LgBill bill ) {
        Logger.error( " pre bills: %d", bills.size() );
        bills.add( bill );
        Logger.error( " post bills: %d", bills.size() );
    }

    public static LgBatch MakeRandom( LgDeposit deposit ) {
        MoneyUnit billLov = MoneyUnit.findByTextId( "Pesos Argentinos" );
        if ( billLov == null ) {
            Logger.error( "no bill code for pesos argentinos!" );
            return null;
        }
        int pesos = billLov.numericId;
        LgBatch batch = new LgBatch();
        LgBillType billType = LgBillType.find( 5, pesos );
        //thisb.save();
        for ( int i = 0; i < 4; i = i + 1 ) {
            int slotid = i;
            LgBill bill = new LgBill( batch, slotid, i, billType, deposit );
            Logger.info( " created: %s", bill.toString() );
            //bill.save();
        }
        Logger.error( " bills: %d", batch.bills.size() );
        return batch;
    }

    @Override
    public String toString() {
        String result = "[";
        for ( LgBill bill : bills ) {
            result = result + " " + bill.toString();
        }
        return result + "]";
    }
}
