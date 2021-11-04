package com.tambapps.http.hyperpoet.json;

import static com.tambapps.http.hyperpoet.util.Ansi.BLUE_SKY;
import static com.tambapps.http.hyperpoet.util.Ansi.GREEN;
import static com.tambapps.http.hyperpoet.util.Ansi.RED;
import static com.tambapps.http.hyperpoet.util.Ansi.RESET;

import groovy.json.DefaultJsonGenerator;
import org.apache.groovy.json.internal.CharBuf;

import java.util.Iterator;
import java.util.Map;

public class PrettyJsonGenerator extends DefaultJsonGenerator {

  private static final int INDENT_SIZE = 2;
  private String indent = "";

  public PrettyJsonGenerator() {
    super(new Options().timezone("UTC"));
  }

  @Override
  protected void writeNumber(Class<?> numberClass, Number value, CharBuf buffer) {
    buffer.addString(RED);
    super.writeNumber(numberClass, value, buffer);
    buffer.addString(RESET);
  }

  @Override
  protected void writeObject(String key, Object object, CharBuf buffer) {
    if (object == null) {
      buffer.addString(RED);
      buffer.addNull();
      buffer.addString(RESET);
    } else if (object instanceof Boolean) {
      buffer.addString(RED);
      buffer.addBoolean((Boolean) object);
      buffer.addString(RESET);
    } else {
      super.writeObject(key, object, buffer);
    }
  }

  @Override
  protected void writeArray(Class<?> arrayClass, Object array, CharBuf buffer) {
    super.writeArray(arrayClass, array, buffer);
  }

  @Override
  protected void writeIterator(Iterator<?> iterator, CharBuf buffer) {
    if (!iterator.hasNext()) {
      buffer.addString("[]");
      return;
    }
    buffer.addString("[\n");
    incrIndent();
    while (iterator.hasNext()) {
      Object it = iterator.next();
      if (!isExcludingValues(it)) {
        buffer.addString(indent);
        writeObject(it, buffer);
        if (iterator.hasNext()) {
          buffer.addString(",");
        }
        buffer.addChar('\n');
      }
    }
    decrIndent();
    buffer.addString(indent + ']');
  }

  private void incrIndent() {
    indent += new String(new char[INDENT_SIZE]).replace('\0', ' ');
  }

  private void decrIndent() {
    indent = indent.substring(2);
  }

  @Override
  protected void writeMap(Map<?, ?> map, CharBuf buffer) {
    if (map.isEmpty()) {
      buffer.addString(indent + "{}");
      return;
    }
    buffer.addString("{\n");
    incrIndent();
    Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<?, ?> entry = iterator.next();
      if (entry.getKey() == null) {
        throw new IllegalArgumentException("Maps with null keys can't be converted to JSON");
      }
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      if (isExcludingValues(value) || isExcludingFieldsNamed(key)) {
        continue;
      }
      writeMapEntry(key, value, buffer);
      if (iterator.hasNext()) {
        buffer.addString(",");
      }
      buffer.addChar('\n');
    }
    decrIndent();
    buffer.addString(indent + "}");
  }

  @Override
  protected void writeMapEntry(String key, Object value, CharBuf buffer) {
    buffer.addString(BLUE_SKY);
    buffer.addString(indent);
    buffer.addQuoted(key);
    buffer.addString(RESET);
    buffer.addString(": ");
    writeObject(key, value, buffer);
  }

  @Override
  protected void writeCharSequence(CharSequence seq, CharBuf buffer) {
    buffer.addString(GREEN);
    super.writeCharSequence(seq, buffer);
    buffer.addString(RESET);
  }

}
