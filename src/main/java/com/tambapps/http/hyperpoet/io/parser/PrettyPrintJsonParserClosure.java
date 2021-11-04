package com.tambapps.http.hyperpoet.io.parser;

import static com.tambapps.http.hyperpoet.util.Ansi.print;
import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.json.PrettyJsonGenerator;
import groovy.json.JsonSlurper;
import lombok.AllArgsConstructor;
import okhttp3.ResponseBody;

import java.io.IOException;

@AllArgsConstructor
public class PrettyPrintJsonParserClosure extends ParserClosure {

  private final JsonSlurper slurper;
  private final PrettyJsonGenerator jsonGenerator;

  public PrettyPrintJsonParserClosure() {
    this(new JsonSlurper(), new PrettyJsonGenerator());
  }

  @Override
  public Object doCall(ResponseBody body) throws IOException {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    Object object = slurper.parseText(text);
    print(jsonGenerator.toJson(object));
    println();
    return object;
  }
}
