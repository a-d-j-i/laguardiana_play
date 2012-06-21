package interfaces;

//import play.*;
//import play.mvc.*;

//import java.util.*;
//import models.*;



import java.util.*;
import play.libs.*;
import play.libs.F.*;
import models.*;

//for testing porposes
import java.util.Random;


public class FakeGlory {

    public static int count() {
        return 5;
    }

    public static List<F.Tuple<CCurrency, Integer>> retrieveCount() {
        Random generator = new Random(); //15151414 XXX TO DO fixed seed!!!
        List<F.Tuple<CCurrency, Integer>> to_deposit = new ArrayList<F.Tuple<CCurrency, Integer>>();
        
        List<CCurrency> all_currencies = CCurrency.all().fetch();
        int _max_cur = all_currencies.size(); 
        
        for (int idx=0; idx<generator.nextInt(10); idx++)
        {
            CCurrency someCurrency = all_currencies.get(generator.nextInt(_max_cur));
            Integer ammount = generator.nextInt(12)+1;
            to_deposit.add(new F.Tuple<CCurrency, Integer> (someCurrency, ammount));
        }
        
        return to_deposit;
    }

}
