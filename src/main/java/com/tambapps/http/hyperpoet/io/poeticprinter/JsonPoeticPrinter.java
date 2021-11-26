package com.tambapps.http.hyperpoet.io.poeticprinter;

import static com.tambapps.http.hyperpoet.util.Ansi.print;

import com.tambapps.http.hyperpoet.io.json.PrettyJsonGenerator;
import groovy.json.JsonSlurper;

public class JsonPoeticPrinter implements PoeticPrinter {

  private final JsonSlurper jsonSlurper = new JsonSlurper();
  private final PrettyJsonGenerator prettyJsonGenerator = new PrettyJsonGenerator();

  @Override
  public void printBytes(byte[] bytes) {
    Object object = jsonSlurper.parseText(new String(bytes));
    print(prettyJsonGenerator.toJson(object));
  }

}
