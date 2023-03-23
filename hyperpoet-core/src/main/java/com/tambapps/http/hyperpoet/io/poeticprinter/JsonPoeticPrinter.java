package com.tambapps.http.hyperpoet.io.poeticprinter;

import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.fasterxml.jackson.databind.JsonNode;
import com.tambapps.http.hyperpoet.io.IoUtils;
import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class JsonPoeticPrinter extends AbstractPoeticPrinter {

  private final JsonGenerator jsonGenerator;

  @SneakyThrows
  @Override
  public void printBytes(byte[] bytes) {
    JsonNode jsonNode = jsonGenerator.getMapper().readValue(bytes, JsonNode.class);
    println(IoUtils.unescapeJavaScript(jsonGenerator.composeToPrettyColoredJson(jsonNode)));
  }

}
