/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import devices.DeviceFactory;
import devices.glory.manager.GloryManager;
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
    // Bill type id
    public Integer tid;
    // Bill type description
    public String btd;
    // Denomination
    public Integer d;
    // Desired Quantity
    public Integer dq = 0;
    // Quantity
    public Integer q = 0;

    static public List<Bill> getCurrentCounters(Integer currency) {
        List<Bill> ret = new ArrayList<Bill>();
        List<LgBillType> billTypes = LgBillType.find(currency);


        Map<Integer, Integer> desiredQuantity = null;
        Map<Integer, Integer> currentQuantity = null;
        GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            currentQuantity = manager.getCurrentQuantity();
            desiredQuantity = manager.getDesiredQuantity();
        }
        for (LgBillType bb : billTypes) {
            Bill b = new Bill();
            b.billType = bb;
            b.tid = bb.billTypeId;
            b.btd = bb.toString();
            b.d = bb.denomination;
            if (currentQuantity != null && currentQuantity.containsKey(bb.slot)) {
                b.q = currentQuantity.get(bb.slot);
            }
            if (desiredQuantity != null && desiredQuantity.containsKey(bb.slot)) {
                b.dq = desiredQuantity.get(bb.slot);
            }
            ret.add(b);
        }
        return ret;
    }
}
