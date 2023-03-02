package com.tambapps.http.hyperpoet.io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

public class CustomJsonGenerator {
  private final ObjectMapper mapper = new ObjectMapper();

  public CustomJsonGenerator() {
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @SneakyThrows
  public String composeToJson(Object o) {
    return mapper.writeValueAsString(o);
  }
}
