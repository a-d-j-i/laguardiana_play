package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;

 
@Entity
public class Deposit extends Model {
    @ManyToOne
    @Required
    public User user;
    
    @Required
    public Date date;
    
    @OneToMany(mappedBy="deposit", cascade=CascadeType.ALL)
    public List<CountList> ditems;
    
    public Deposit(User user) {
        this.user = user;
        this.date = new Date();
        this.ditems = new ArrayList<CountList>();
    }
 
 
    public Deposit addCountList(CountList clist) {
        this.ditems.add(clist);
        clist.save();
        return this;
    }
    
    public Integer value() {
        Integer value = new Integer(0);
        for (int idx=0; idx<this.ditems.size(); idx++) {
            value += this.ditems.get(idx).value();
        }
        return value;
    }
    
}
