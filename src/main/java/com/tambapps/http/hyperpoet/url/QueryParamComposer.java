package com.tambapps.http.hyperpoet.url;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public class QueryParamComposer {

  private final Map<Class<?>, Closure<?>> converters;
  private MultivaluedQueryParamComposingType multivaluedQueryParamComposingType;

  public Closure<?> getAt(Class<?> clazz) {
    return converters.get(clazz);
  }

  public <T> void putAt(Class<T> clazz,
      @ClosureParams(FirstParam.FirstGenericType.class) Closure<String> closure) {
    converters.put(clazz, closure);
  }

  public List<QueryParam> compose(Object value) {
    if (value instanceof Map) {
      return compose((Map) value);
    } else {
      Map<?, ?> map = DefaultGroovyMethods.getProperties(value);
      // groovy creates a property for the class. we don't want that
      map.remove("class");
      return compose(map);
    }
  }

  public List<QueryParam> compose(Map<?, ?> value) {
    return value.entrySet()
        .stream()
        .map(e -> compose(String.valueOf(e.getKey()), e.getValue()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public List<QueryParam> compose(String name, Object value) {
    if (value instanceof Collection) {
      return composeCollectionParam(name, (Collection) value);
    } else {
      return Collections.singletonList(new QueryParam(name, composeParam(value)));
    }
  }

  private List<QueryParam> composeCollectionParam(String name, Collection<?> collection) {
    List<QueryParam> queryParams = new ArrayList<>();
    switch (multivaluedQueryParamComposingType) {
      case COMMA:
      case BRACKETS:
        StringBuilder bracketsListBuilder = new StringBuilder();
        if (multivaluedQueryParamComposingType == MultivaluedQueryParamComposingType.BRACKETS) {
          bracketsListBuilder.append('[');
        }
        bracketsListBuilder.append(collection.stream().map(String::valueOf).collect(Collectors.joining(",")));
        if (multivaluedQueryParamComposingType == MultivaluedQueryParamComposingType.BRACKETS) {
          bracketsListBuilder.append(']');
        }
        queryParams.add(new QueryParam(name, bracketsListBuilder.toString()));
        break;
      case REPEAT:
        for (Object value : collection) {
          queryParams.addAll(compose(name, value));
        }
        break;
    }
    return queryParams;
  }

  private String composeParam(Object value) {
    if (value == null) {
      return "null";
    }
    Closure<?> converter = findConverter(value.getClass());
    return converter != null ? String.valueOf(converter.call(value)) : String.valueOf(value);
  }

  private Closure<?> findConverter(Class<?> clazz) {
    return converters.entrySet()
        .stream()
        .filter(e -> e.getKey().isAssignableFrom(clazz))
        .map(Map.Entry::getValue).findFirst().orElse(null);
  }
}
