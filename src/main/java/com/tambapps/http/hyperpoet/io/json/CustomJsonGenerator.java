package com.tambapps.http.hyperpoet.io.json;

import com.tambapps.http.hyperpoet.Function;
import groovy.json.DefaultJsonGenerator;
import groovy.lang.Closure;
import lombok.AllArgsConstructor;

public class CustomJsonGenerator extends DefaultJsonGenerator {

  public CustomJsonGenerator() {
    super(new Options());
  }

  public void addConverter(Class<?> clazz, Function closure) {
    converters.add(new TypeConverter(clazz, closure));
  }

  public Function getAt(Class<?> clazz) {
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

  public void putAt(Class<?> clazz, Function closure) {
    addConverter(clazz, closure);
  }

  @AllArgsConstructor
  private static class TypeConverter implements Converter {

    private final Class<?> clazz;
    private final Function closure;

    @Override
    public boolean handles(Class<?> type) {
      return this.clazz.isAssignableFrom(type);
    }

    @Override
    public Object convert(Object value, String key) {
      return closure.call(value);
    }
  }
}
