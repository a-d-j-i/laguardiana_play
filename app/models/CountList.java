package models;
 
import java.util.*;
import javax.persistence.*;
import java.util.*;

import play.libs.*;
import play.libs.F.*;
 
import play.db.jpa.*;
import play.data.validation.*;

 
@Entity
public class CountList extends Model {
    @ManyToOne
    @Required
    public User user;
    
    @Required
    public Date date;
    
    @ManyToOne
    public Deposit deposit;
    
    @OneToMany(mappedBy="countList", cascade=CascadeType.ALL)
    public List<DepositItem> citems;
    
    public CountList(User user) {
        this.date = new Date();
        this.user = user;
        this.citems = new ArrayList<DepositItem>();
    }

    public CountList(User user, Deposit deposit) {
        this.date = new Date();
        this.user = user;
        this.deposit = deposit;
        this.citems = new ArrayList<DepositItem>();
    }
    
    public CountList addFromList(List<F.Tuple<CCurrency, Integer>> dilist) {
        for (int idx=0; idx<dilist.size(); idx++)
        {
            F.Tuple<CCurrency, Integer> di_tuple = dilist.get(idx);
            this.addDepositItem(di_tuple._1, di_tuple._2);
        }
        return this;
    }
 
    public CountList addDepositItem(CCurrency currency, Integer quantity) {
        DepositItem di = new DepositItem(this, currency, quantity).save();
        this.citems.add(di);
        di.save();
        return this;
    }
    
    public Integer value() {
        Integer value = new Integer(0);
        for (int idx=0; idx<this.citems.size(); idx++) {
            value += this.citems.get(idx).value();
        }
        return value;
    }
    
}
