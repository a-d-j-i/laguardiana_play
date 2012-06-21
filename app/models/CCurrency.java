package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;
 
@Entity
public class CCurrency extends Model {
    @Required
    public String name;
    @Required
    public Date validFrom;
    public Date validTo;
    @Required
    public boolean expired;
    @Required
    public Integer convRate;
    
    public CCurrency(String name, Integer convRate) {
        this.name = name;
        this.validFrom = new Date();
        this.expired = false;
        this.validTo = null;
        this.convRate = convRate;
    }
    
    public void expire() {
        this.expired = true;
        this.validTo = new Date();
        this.save();
    }
    
    public Integer getValue(Integer quantity) {
        return this.convRate * quantity;
    }
}
