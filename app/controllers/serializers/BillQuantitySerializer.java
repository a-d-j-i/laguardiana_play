package controllers.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import models.BillQuantity;

/**
 *
 * @author adji
 */
public class BillQuantitySerializer implements JsonSerializer<BillQuantity> {

    public JsonElement serialize(BillQuantity t, Type type, JsonSerializationContext jsc) {
        JsonObject ret = new JsonObject();
        ret.addProperty("tid", t.getKey());
        ret.addProperty("d", t.getDenomination());
        ret.addProperty("q", t.getQuantity());
        return ret;
    }
}
