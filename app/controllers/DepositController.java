package controllers;

import models.Deposit;
import models.db.LgLov;
import models.db.LgUser;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

@With( Secure.class)
public class DepositController extends Controller {

    public static void index() {
        Application.index();
    }

    public static Deposit createDeposit(String reference1, String reference2) throws Throwable {
        //TODO: Validate references depending on system properties. 
        if (reference1 != null && reference2 != null) {
            LgUser user = Secure.getCurrentUser();
            Integer ref1 = Integer.parseInt(reference1);
            LgLov userCode = DepositUserCodeReference.findByNumericId(ref1);
            if (userCode == null) {
                Logger.error("countMoney: no reference received! for %s", reference1);
                index();
                return null;
            }

            Deposit deposit = new Deposit(user, reference2, userCode);
            deposit.save();
            return deposit;
        }
        return null;
    }
}
