package com.tambapps.http.hyperpoet.io;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// TODO remove this or rename it into Formatters
//  formatters should be used in composers and when composing query params
/**
 * Utility class to handle several common query param composers
 * A query param composer should return a String or a Collection of String
 */
public class QueryParamComposers {

  public static String composeLocalDate(LocalDate localDate) {
    return DateTimeFormatter.ISO_DATE.format(localDate);
  }

  public static String composeLocalDateTime(LocalDateTime localDateTime) {
    return DateTimeFormatter.ISO_INSTANT.format(localDateTime.toInstant(ZoneOffset.UTC));
  }

  public static Map<Class<?>, Closure<?>> getMap() {
    Map<Class<?>, Closure<?>> map = new HashMap<>();
    map.put(LocalDate.class, new MethodClosure(QueryParamComposers.class, "composeLocalDate"));
    map.put(LocalDateTime.class, new MethodClosure(QueryParamComposers.class, "composeLocalDateTime"));
    return map;
  }
}
