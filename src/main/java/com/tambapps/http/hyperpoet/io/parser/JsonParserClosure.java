package com.tambapps.http.hyperpoet.io.parser;

import groovy.json.JsonSlurper;
import okhttp3.ResponseBody;

import java.io.IOException;

public class JsonParserClosure extends ParserClosure {

  private final JsonSlurper slurper;
  public JsonParserClosure() {
    this(new JsonSlurper());
  }
  public JsonParserClosure(JsonSlurper slurper) {
    this.slurper = slurper;
  }

  @Override
  public Object doCall(ResponseBody body) throws IOException {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    return slurper.parseText(text);
  }
}
