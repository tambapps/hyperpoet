package com.tambapps.http.hyperpoet.util;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.Function;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

/**
 * A map of content type -&gt; T that handles content types inclusion when retrieving an element
 */
@NoArgsConstructor
// using TreeMap to have ContentType sorted when iterating over keys and checking inclusion
public class ContentTypeMapFunction extends TreeMap<ContentType, Function> {

  @Getter
  @Setter
  private Function defaultValue;

  public ContentTypeMapFunction(Map<ContentType, Function> map) {
    super(map);
  }

  public Function putAt(ContentType contentType, Function value) {
    return put(contentType, value);
  }

  @Override
  public Function getOrDefault(Object key, Function defaultValue) {
    Function v;
    return (((v = super.get(key)) != defaultValue) || containsKey(key))
        ? v
        : defaultValue;
  }

  @Override
  public Function get(Object key) {
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
