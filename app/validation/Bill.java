/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import models.db.LgBillType;

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

}
