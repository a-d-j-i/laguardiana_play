/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.dbcrud;

import controllers.CRUD;
import controllers.Secure;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import models.db.LgLov;
import models.db.LgLov.LovCol;
import play.db.Model;
import play.db.Model.Property;
import play.exceptions.UnexpectedException;
import play.mvc.With;

/**
 *
 * @author adji
 */
@With( Secure.class)
public class CrudBaseController extends CRUD {

    protected static ObjectType createObjectType(Class<? extends Model> type) {
        return new LovObjectType(type);
    }

    public static class EnumClass {

        public String name() {
            return name;
        }

        public Integer ordinal() {
            return ordinal;
        }
        public String name;
        public Integer ordinal;

        public EnumClass(String name, Integer ordinal) {
            this.name = name;
            this.ordinal = ordinal;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class LovObjectType extends ObjectType {

        private final HashMap<String, Class> lovCols = new HashMap<String, Class>();

        public LovObjectType(Class<? extends Model> modelClass) {
            super(modelClass);
            Class c = modelClass;
            try {
                while (!c.equals(Object.class)) {
                    for (Field field : c.getDeclaredFields()) {
                        if (field.isAnnotationPresent(LovCol.class)) {
                            lovCols.put(field.getName(), field.getAnnotation(LovCol.class).value());
                        }
                    }
                    c = c.getSuperclass();
                }
            } catch (Exception e) {
                throw new UnexpectedException("Error while determining the "
                        + "object @Version for an object of type " + modelClass);
            }
        }

        public class LovObjectField extends ObjectField {

            private Model.Property property;
            private Class lovClass;

            private LovObjectField(Property property, Class lovClass) {
                super(property);
                this.property = property;
                this.lovClass = lovClass;
            }

            @Override
            public List<Object> getChoices() {
                List<LgLov> ll = LgLov.find("select l from LgLov l where l.class = ?", lovClass.getSimpleName()).fetch();
                List<Object> ret = new ArrayList<Object>();
                for (LgLov l : ll) {
                    ret.add(new EnumClass(l.description, l.numericId));
                }
                return ret;
            }
        }

        @Override
        public List<ObjectField> getFields() {
            List<ObjectField> fields = new ArrayList<ObjectField>();
            List<ObjectField> hiddenFields = new ArrayList<ObjectField>();
            for (play.db.Model.Property f : factory.listProperties()) {
                ObjectField of;
                if (lovCols.containsKey(f.field.getName())) {
                    of = new LovObjectField(f, lovCols.get(f.field.getName()));
                    of.type = "enum";
                } else {
                    of = new ObjectField(f);
                }
                if (of.type != null) {
                    if (of.type.equals("hidden")) {
                        hiddenFields.add(of);
                    } else {
                        fields.add(of);
                    }
                }
            }

            hiddenFields.addAll(fields);
            return hiddenFields;
        }
    }

    public static void index() {
        render("CRUD/index.html");
    }
}
