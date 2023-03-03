package com.tambapps.http.hyperpoet;

import lombok.Getter;

import java.util.AbstractList;
import java.util.LinkedList;

public class History extends AbstractList<HttpExchange> {

  @Getter
  private int limit;
  private final LinkedList<HttpExchange> exchanges;

  public History(int limit) {
    this.limit = limit;
    this.exchanges = new LinkedList<>();
    validateLimit(limit);
  }

  @Override
  public boolean add(HttpExchange httpExchange) {
    if (exchanges.size() >= limit) {
      exchanges.removeFirst();
    }
    return exchanges.add(httpExchange);
  }

  @Override
  public HttpExchange get(int index) {
    // to allow getting with reverse index
    return exchanges.get(index >= 0 ? index : size() - index);
  }

  @Override
  public int size() {
    return exchanges.size();
  }

  public void setLimit(int limit) {
    validateLimit(limit);
    this.limit = limit;
    while (exchanges.size() > limit) {
      exchanges.removeFirst();
    }
  }

  private void validateLimit(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be greater than 0");
    }
  }
}
