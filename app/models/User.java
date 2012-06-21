package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;
 
@Entity
public class User extends Model {
    @Required
    public String email;
    
    @Required
    public String password;
    public String fullname;
    public boolean isAdmin;
    
    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }
    
    public static User connect(String email, String password) {
        /*System.out.println("searching for");
        System.out.println(email);
        System.out.println(password);
        System.out.println("");*/
        return find("byEmailAndPassword", email, password).first();
    }
 
}
