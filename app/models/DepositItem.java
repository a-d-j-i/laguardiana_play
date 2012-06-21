package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;

 
@Entity
public class DepositItem extends Model {
    @Required
    @ManyToOne
    public CountList countList;
    
    @Required
    @ManyToOne
    public CCurrency currency;
    
    @Required
    public Integer quantity;
    
    
    public DepositItem(CountList countList, CCurrency currency, Integer quantity) {
        this.countList = countList;
        this.currency = currency;
        this.quantity = quantity;
    }
    
    public Integer value() {
        return this.currency.getValue(this.quantity);
    }
}
