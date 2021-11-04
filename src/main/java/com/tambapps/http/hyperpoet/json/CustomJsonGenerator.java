package com.tambapps.http.hyperpoet.json;

import groovy.json.DefaultJsonGenerator;
import groovy.lang.Closure;
import lombok.AllArgsConstructor;

public class CustomJsonGenerator extends DefaultJsonGenerator {

  public CustomJsonGenerator() {
    super(new Options());
  }

  public void addConverter(Class<?> clazz, Closure<?> closure) {
    converters.add(new TypeConverter(clazz, closure));
  }

  public Closure<?> getAt(Class<?> clazz) {
    for (Converter converter : converters) {
      if (!(converter instanceof TypeConverter)) {
        continue;
      }
      TypeConverter typeConverter = (TypeConverter) converter;
      if (typeConverter.handles(clazz)) {
        return typeConverter.closure;
      }
    }
    return null;
  }

  // TODO document it
  public void putAt(Class<?> clazz, Closure<?> closure) {
    addConverter(clazz, closure);
  }

  @AllArgsConstructor
  private static class TypeConverter implements Converter {

    private final Class<?> clazz;
    private final Closure<?> closure;

    @Override
    public boolean handles(Class<?> type) {
      return this.clazz.isAssignableFrom(type);
    }

    @Override
    public Object convert(Object value, String key) {
      if (closure.getMaximumNumberOfParameters() > 1) {
        return closure.call(value, key);
      } else {
        return closure.call(value);
      }
    }
  }
}
