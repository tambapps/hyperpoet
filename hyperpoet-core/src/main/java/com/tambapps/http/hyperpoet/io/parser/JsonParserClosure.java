package com.tambapps.http.hyperpoet.io.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParserClosure extends ParserClosure {

  private final ObjectMapper mapper;
  public JsonParserClosure() {
    this(new ObjectMapper());
  }
  public JsonParserClosure(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Object doCall(ResponseBody body) throws IOException {

    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }

    JsonNode node = mapper.readTree(text);
    return toObject(node);
  }

  private Object toObject(JsonNode node) {
    if (node.isDouble()) return node.asDouble();
    else if (node.isFloat()) return node.floatValue();
    else if (node.isLong()) return node.asLong();
    else if (node.isInt()) return node.asInt();
    else if (node.isTextual()) return node.asText();
    else if (node.isArray()) {
      List<Object> list = new ArrayList<>();
      for (int i = 0; i < node.size(); i++) {
        list.add(toObject(node.get(i)));
      }
      return list;
    } else if (node.isEmpty()) {
      return new HashMap<>();
    } else if (node.isObject()) {
      Map<String, Object> map = new HashMap<>();
      node.fields().forEachRemaining(e -> map.put(e.getKey(), toObject(e.getValue())));
      return map;
    } else {
      throw new RuntimeException("Internal error, doesn't handle such node");
    }
  }
}
