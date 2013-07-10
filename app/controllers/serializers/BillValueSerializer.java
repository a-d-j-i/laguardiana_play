package controllers.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import models.BillValue;

/**
 *
 * @author adji
 */
public class BillValueSerializer implements JsonSerializer<BillValue> {

    public JsonElement serialize(BillValue t, Type type, JsonSerializationContext jsc) {
        JsonObject ret = new JsonObject();
        ret.addProperty("c", t.currency.description);
        ret.addProperty("d", t.denomination);
        return ret;
    }
}
