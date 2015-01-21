import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class BasicTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }
 
    @Test
    public void createAndRetrieveUser() {
        System.out.println("1");
        // Create a new user and save it
        //new User("bob@gmail.com", "secret", "Bob").save();
        
        // Retrieve the user with e-mail address bob@gmail.com
        //User bob = User.find("byEmail", "bob@gmail.com").first();
        
        // Test 
        //assertNotNull(bob);
        //assertEquals("Bob", bob.fullname);
    }
    
    @Test
    public void tryConnectAsUser() {
        // Create a new user and save it
        //new User("bob@gmail.com", "secret", "Bob").save();
        
        // Test 
        //assertNotNull(User.connect("bob@gmail.com", "secret"));
        //assertNull(User.connect("bob@gmail.com", "badpassword"));
        //assertNull(User.connect("tom@gmail.com", "secret"));
    }
    
    @Test
    public void checkCurrencies() {
        // Create a new user and save it
        /*CCurrency pesoarg1 = new CCurrency("peso1", 1).save();
        CCurrency pesoarg10 = new CCurrency("peso10", 10).save();
        CCurrency pesoarg20 = new CCurrency("peso20", 20).save();

        assertEquals(false, pesoarg1.expired);     
        assertEquals(false, pesoarg10.expired);     
        assertEquals(false, pesoarg20.expired);     
        assertEquals((Integer)1, pesoarg1.convRate);     
        assertEquals((Integer)10, pesoarg10.convRate);     
        assertEquals((Integer)20, pesoarg20.convRate);
        
        pesoarg10.expire();
        assertEquals(true, pesoarg10.expired);*/
    }
    
    @Test
    public void testDepositAndDepositItems() {/*
        User user1 = new User("bobo@gmail.com", "secreto", "Bobo").save();
        User bobo = User.find("byEmail", "bobo@gmail.com").first();
        
        CCurrency pesoarg1 = new CCurrency("peso1", 1).save();
        CCurrency pesoarg10 = new CCurrency("peso10", 10).save();
        CCurrency pesoarg20 = new CCurrency("peso20", 20).save();

        CountList countlist = new CountList(bobo);
        countlist.save();
        
        List<F.Tuple<CCurrency, Integer>> to_deposit = new ArrayList<F.Tuple<CCurrency, Integer>>();
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg1, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg10, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg20, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg10, 6));

        
        for (int i=0; i<to_deposit.size(); i++){
            F.Tuple<CCurrency, Integer> current_item = to_deposit.get(i);
            countlist.addDepositItem(current_item._1, (Integer)current_item._2);
        }

        List<DepositItem> boboDepositItems = DepositItem.find("byCountList", countlist).fetch();
        assertEquals(to_deposit.size(), boboDepositItems.size());
        
        assertEquals(to_deposit.size(), countlist.citems.size()); 

        assertEquals((Integer)(3+3*10+3*20+6*10), countlist.value()); 
      
        */
    }

    @Test
    public void testDeposit() {
/*        User user1 = new User("bobo@gmail.com", "secreto", "Bobo").save();
        User bobo = User.find("byEmail", "bobo@gmail.com").first();
        
        CCurrency pesoarg1 = new CCurrency("peso1", 1).save();
        CCurrency pesoarg10 = new CCurrency("peso10", 10).save();
        CCurrency pesoarg20 = new CCurrency("peso20", 20).save();


        Deposit deposit = new Deposit(bobo);
        deposit.save();
        
        List<F.Tuple<CCurrency, Integer>> to_deposit = new ArrayList<F.Tuple<CCurrency, Integer>>();
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg1, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg10, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg20, 3));
        to_deposit.add(new F.Tuple<CCurrency, Integer> (pesoarg10, 6));

        
        for (int i=0; i<to_deposit.size(); i++){
            F.Tuple<CCurrency, Integer> current_item = to_deposit.get(i);
            deposit.addDepositItem(current_item._1, (Integer)current_item._2);
        }

        List<DepositItem> boboDepositItems = DepositItem.find("byDeposit", deposit).fetch();
        assertEquals(to_deposit.size(), boboDepositItems.size());
        
        assertEquals(to_deposit.size(), deposit.ditems.size()); 

        assertEquals((Integer)(3+3*10+3*20+6*10), deposit.value()); */
    }
}











