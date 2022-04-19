package com.tambapps.http.hyperpoet.util;

import com.tambapps.http.hyperpoet.ContentType;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

/**
 * A map of content type -> T that handles content types inclusion when retrieving an element
 * @param <T>
 */
@NoArgsConstructor
// using TreeMap to have ContentType sorted when iterating over keys and checking inclusion
public class ContentTypeMap<T> extends TreeMap<ContentType, T> {

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
    return null;
  }
}
