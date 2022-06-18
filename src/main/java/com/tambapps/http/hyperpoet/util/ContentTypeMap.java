package com.tambapps.http.hyperpoet.util;

import com.tambapps.http.hyperpoet.ContentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

/**
 * A map of content type -&gt; T that handles content types inclusion when retrieving an element
 * @param <T> the type of the map values
 */
@NoArgsConstructor
// using TreeMap to have ContentType sorted when iterating over keys and checking inclusion
public class ContentTypeMap<T> extends TreeMap<ContentType, T> {

  @Getter
  @Setter
  private T defaultValue;

  public ContentTypeMap(Map<ContentType, T> map) {
    super(map);
  }

  public T putAt(ContentType contentType, T value) {
    return put(contentType, value);
  }

  @Override
  public T get(Object key) {
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

  public T getAt(ContentType contentType) {
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
