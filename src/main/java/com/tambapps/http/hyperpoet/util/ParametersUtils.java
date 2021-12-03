package com.tambapps.http.hyperpoet.util;

import java.util.Map;
import java.util.function.Supplier;

public class ParametersUtils {

  public static <T> T getOrDefault(Map<?, ?> additionalParameters, String key, Class<T> clazz,
      T defaultValue) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValue;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(
          String.format("Unexpected type for parameter '%s', expected type %s", key,
              clazz.getSimpleName()));
    }
    return (T) object;
  }

  public static <T> T getOrDefaultSupply(Map<?, ?> additionalParameters, String key,
      Class<T> clazz, Supplier<T> defaultValueSupplier) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValueSupplier.get();
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(
          String.format("Unexpected type for parameter '%s', expected type %s", key,
              clazz.getSimpleName()));
    }
    return (T) object;
  }
}
