/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.db.LgBillType;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class Bill {

    public LgBillType billType;
    public Integer quantity = 0;

    static public List<Bill> getCurrentCounters() {
        List<Bill> ret = new ArrayList<Bill>();
        List<LgBillType> billTypes = LgBillType.findAll();

        Map<Integer, Integer> billData = null;
        Manager.ControllerApi manager = CounterFactory.getManager( Play.configuration.getProperty( "glory.port" ) );
        if ( manager != null ) {
            billData = manager.getBillData();
        }
        for ( LgBillType bb : billTypes ) {
            Bill b = new Bill();
            b.billType = bb;
            if ( billData != null && billData.containsKey( bb.slot ) ) {
                b.quantity = billData.get( bb.slot );
            }
            ret.add( b );
        }
        return ret;
    }

    static public int[] getSlotArray( Map<String, String> billTypeIds ) {
        int[] bills = new int[ 32 ];

        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        if ( billTypeIds != null ) {
            for ( String k : billTypeIds.keySet() ) {
                LgBillType b = LgBillType.findById( Integer.parseInt( k ) );
                if ( b != null && b.slot > 0 ) {
                    if ( b.slot >= bills.length ) {
                        Logger.error( String.format( "getSlotArray Invalid slot %d", b.slot ) );
                    }
                    bills[ b.slot] = Integer.parseInt( billTypeIds.get( k ) );
                }
            }
        }
        return bills;
    }
}
