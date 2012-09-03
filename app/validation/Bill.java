/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.db.LgBillType;
import play.Play;

/**
 * @author adji
 */
public class Bill {

    transient public LgBillType billType;
    public Integer billTypeId;
    public String billTypeDescription;
    public Integer denomination;
    public Integer desiredQuantity = 0;
    public Integer quantity = 0;

    static public List<Bill> getCurrentCounters(Integer currency) {
        List<Bill> ret = new ArrayList<Bill>();
        List<LgBillType> billTypes = LgBillType.find(currency);


        Map<Integer, Integer> desiredQuantity = null;
        Map<Integer, Integer> currentQuantity = null;
        Manager.ControllerApi manager = CounterFactory.getManager(Play.configuration.getProperty("glory.port"));
        if (manager != null) {
            currentQuantity = manager.getCurrentQuantity();
            desiredQuantity = manager.getDesiredQuantity();
        }
        for (LgBillType bb : billTypes) {
            Bill b = new Bill();
            b.billType = bb;
            b.billTypeId = bb.billTypeId;
            b.billTypeDescription = bb.toString();
            b.denomination = bb.denomination;
            if (currentQuantity != null && currentQuantity.containsKey(bb.slot)) {
                b.quantity = currentQuantity.get(bb.slot);
            }
            if (desiredQuantity != null && desiredQuantity.containsKey(bb.slot)) {
                b.desiredQuantity = desiredQuantity.get(bb.slot);
            }
            ret.add(b);
        }
        return ret;
    }
}
