package com.tambapps.http.hyperpoet.io.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.skjolber.jackson.jsh.AnsiSyntaxHighlight;
import com.github.skjolber.jackson.jsh.DefaultSyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlightingJsonGenerator;
import com.tambapps.http.hyperpoet.io.IoUtils;
import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.StringWriter;
import java.util.Map;

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

  public <T> void addSerializer(Class<T> tClass, JsonSerializer<T> serializer) {
    SimpleModule module = new SimpleModule();
    module.addSerializer(tClass, serializer);
    mapper.registerModule(module);
  }

  @SneakyThrows
  public String composeToJson(Object o) {
    return mapper.writeValueAsString(o);
  }

  @SneakyThrows
  // is String, byte[] or inputStream
  public String composeRawDataToPrettyColoredJson(Object rawInput) {
    String json = IoUtils.rawToString(rawInput);
    JsonNode jsonNode = mapper.readValue(json, JsonNode.class);
    return composeToPrettyColoredJson(jsonNode);
  }

  @SneakyThrows
  public String composeToPrettyColoredJson(JsonNode jsonNode) {
    StringWriter writer = new StringWriter();
    com.fasterxml.jackson.core.JsonGenerator jsonGenerator = new SyntaxHighlightingJsonGenerator(mapper.createGenerator(writer), highlighter, true);
    jsonGenerator.writeObject(JsonParserClosure.toObject(jsonNode));
    writer.flush();
    return writer.toString();
  }
}
