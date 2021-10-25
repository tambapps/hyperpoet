package com.tambapps.http.hyperpoet.io;

import com.tambapps.http.hyperpoet.util.QueryParam;
import com.tambapps.http.hyperpoet.util.UrlBuilder;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class QueryParamComposer {
  private final UrlBuilder.MultivaluedQueryParamComposingType multivaluedQueryParamComposingType;

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
        if (multivaluedQueryParamComposingType == UrlBuilder.MultivaluedQueryParamComposingType.BRACKETS) {
          bracketsListBuilder.append('[');
        }
        bracketsListBuilder.append(collection.stream().map(String::valueOf).collect(Collectors.joining(",")));
        if (multivaluedQueryParamComposingType == UrlBuilder.MultivaluedQueryParamComposingType.BRACKETS) {
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
    // TODO handle custom composers
    return String.valueOf(value);
  }
}
