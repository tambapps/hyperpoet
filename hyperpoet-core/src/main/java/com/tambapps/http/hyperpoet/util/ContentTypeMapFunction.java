package com.tambapps.http.hyperpoet.util;

import com.tambapps.http.hyperpoet.ContentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * A map of content type -&gt; T that handles content types inclusion when retrieving an element
 */
@NoArgsConstructor
// using TreeMap to have ContentType sorted when iterating over keys and checking inclusion
public class ContentTypeMapFunction extends TreeMap<ContentType, Function<Object, ?>> {

  @Getter
  @Setter
  private Function<Object, ?> defaultValue;

  public ContentTypeMapFunction(Map<ContentType, Function<Object, ?>> map) {
    super(map);
  }

  public Function putAt(ContentType contentType, Function<Object, ?> value) {
    return put(contentType, value);
  }

  @Override
  public Function<Object, ?> getOrDefault(Object key, Function defaultValue) {
    Function<Object, ?> v;
    return (((v = super.get(key)) != defaultValue) || containsKey(key))
        ? v
        : defaultValue;
  }

  @Override
  public Function<Object, ?> get(Object key) {
    if (key instanceof ContentType) {
      return getAt((ContentType) key);
    } else if (key == null) {
      // normally treemap doesn't accept null values.
      // instead we return the default value
      return defaultValue;
    } else {
      return super.get(key);
    }
  }

  public Function getAt(ContentType contentType) {
    if (contentType == null) {
      return super.get(null);
    }
    if (containsKey(contentType)) {
      return super.get(contentType);
    }
    for (ContentType candidateKey : keySet()) {
      if (candidateKey.includes(contentType)) {
        return super.get(candidateKey);
      }
    }
    return defaultValue;
  }
}
