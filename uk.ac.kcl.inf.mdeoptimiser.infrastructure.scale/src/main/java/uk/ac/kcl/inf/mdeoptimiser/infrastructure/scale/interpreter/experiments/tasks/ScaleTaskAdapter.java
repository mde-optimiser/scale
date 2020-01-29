package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ScaleTaskAdapter<T extends IScaleTask> implements JsonSerializer, JsonDeserializer {

  private static final String CLASSNAME = "className";

  @Override
  public Object deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    final JsonObject jsonObject = jsonElement.getAsJsonObject();
    final JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
    final String className = prim.getAsString();
    final Class<T> clazz = getClassInstance(className);
    return context.deserialize(jsonObject, clazz);
  }

  @Override
  public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Class<T> getClassInstance(String className) {
    try {
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      throw new JsonParseException(cnfe.getMessage());
    }
  }

}
