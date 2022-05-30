package com.tambapps.http.hyperpoet.io.poeticprinter;

import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.io.json.PrettyJsonGenerator;
import groovy.json.JsonSlurper;
import groovy.json.StringEscapeUtils;

public class JsonPoeticPrinter extends AbstractPoeticPrinter {

  private final JsonSlurper jsonSlurper = new JsonSlurper();
  private final PrettyJsonGenerator prettyJsonGenerator = new PrettyJsonGenerator();

  @Override
  public void printBytes(byte[] bytes) {
    Object object = jsonSlurper.parseText(new String(bytes));
    println(StringEscapeUtils.unescapeJavaScript(prettyJsonGenerator.toJson(object)));
  }

}
