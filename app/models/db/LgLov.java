package models.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "lg_lov", schema = "public")
@DiscriminatorColumn(name = "type", length = 32)
abstract public class LgLov extends GenericModel implements java.io.Serializable {

    @Target(value = {ElementType.METHOD, ElementType.FIELD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface LovCol {

        Class value();
    }
    @Id
    @Column(name = "lov_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer lovId;
    @Column(name = "type", nullable = false, updatable = false, insertable = false)
    public String type;
    @Column(name = "numeric_id", nullable = false)
    public Integer numericId;
    @Column(name = "text_id", nullable = false, length = 32)
    public String textId;
    @Column(name = "description", nullable = false, length = 256)
    public String description;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true, length = 13)
    public Date endDate;

    static public long count(String ctype, Integer numericId) {
        return LgLov.find("select count(*) from LgLov l where numericId = ? and l.class = ?", numericId, ctype).first();
    }

    static public LgLov findByNumericId(String ctype, Integer numericId) {
        return LgLov.find("select l from LgLov l where numericId = ? and l.class = ?", numericId, ctype).first();
        //return LgLov.find("byType", UserCodeReference).fetch();
    }

    static public LgLov findByTextId(String ctype, String textId) {
        return LgLov.find("select l from LgLov l where textId = ? and l.class = ?", textId, ctype).first();
    }

    @Override
    public String toString() {
        return this.description;
    }
}
