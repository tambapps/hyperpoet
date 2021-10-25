package com.tambapps.http.hyperpoet.io;

import groovy.json.DefaultJsonGenerator;
import groovy.lang.Closure;
import lombok.AllArgsConstructor;

public class PoeticJsonGenerator extends DefaultJsonGenerator {

  public PoeticJsonGenerator() {
    super(new Options());
  }

  public void addConverter(Class<?> clazz, Closure<?> closure) {
    converters.add(new TypeConverter(clazz, closure));
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
