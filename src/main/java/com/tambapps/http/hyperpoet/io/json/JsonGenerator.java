package com.tambapps.http.hyperpoet.io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.skjolber.jackson.jsh.AnsiSyntaxHighlight;
import com.github.skjolber.jackson.jsh.DefaultSyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlightingJsonGenerator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.StringWriter;

public class JsonGenerator {
  @Getter
  private final ObjectMapper mapper = new ObjectMapper();
  private final SyntaxHighlighter highlighter = DefaultSyntaxHighlighter
      .newBuilder()
      .withNumber(AnsiSyntaxHighlight.RED)
      .withString(AnsiSyntaxHighlight.GREEN)
      .withField(AnsiSyntaxHighlight.BLUE)
      .withBoolean(AnsiSyntaxHighlight.RED)
      .withNull(AnsiSyntaxHighlight.RED)
      .build();
  public JsonGenerator() {
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @SneakyThrows
  public String composeToJson(Object o) {
    return mapper.writeValueAsString(o);
  }

  @SneakyThrows
  public String composeToPrettyColoredJson(Object object) {
    StringWriter writer = new StringWriter();
    com.fasterxml.jackson.core.JsonGenerator jsonGenerator = new SyntaxHighlightingJsonGenerator(mapper.createGenerator(writer), highlighter, true);
    jsonGenerator.writeObject(object);
    writer.flush();
    return writer.toString();
  }
}
