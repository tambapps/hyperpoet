package com.tambapps.http.hyperpoet.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

public class IoUtils {

  public static byte[] getBytes(Path path) throws IOException {
    return getBytes(path.toFile());
  }

  public static byte[] getBytes(File file) throws IOException {
    return getBytes(new FileInputStream(file));
  }
  public static byte[] getBytes(InputStream is) throws IOException {
    ByteArrayOutputStream answer = new ByteArrayOutputStream();
    byte[] byteBuffer = new byte[8192];

    int nbByteRead;
    try {
      while((nbByteRead = is.read(byteBuffer)) != -1) {
        answer.write(byteBuffer, 0, nbByteRead);
      }
    } finally {
      closeSilently(is);
    }

    return answer.toByteArray();
  }

  public static String getText(Reader reader) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    return getText(bufferedReader);
  }

  public static String getText(BufferedReader reader) throws IOException {
    StringBuilder answer = new StringBuilder();
    char[] charBuffer = new char[8192];

    try {
      int nbCharRead;
      while((nbCharRead = reader.read(charBuffer)) != -1) {
        answer.append(charBuffer, 0, nbCharRead);
      }

      Reader temp = reader;
      reader = null;
      temp.close();
    } finally {
      closeSilently(reader);
    }

    return answer.toString();
  }

  public static void closeSilently(Closeable closeable) {
    tryClose(closeable);
  }

  static Throwable tryClose(AutoCloseable closeable) {
    Throwable thrown = null;
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception var4) {
        thrown = var4;
      }
    }

    return thrown;
  }


  public static String unescapeJava(String str) {
    if (str == null) {
      return null;
    } else {
      try {
        StringWriter writer = new StringWriter(str.length());
        unescapeJava(writer, str);
        return writer.toString();
      } catch (IOException var2) {
        throw new RuntimeException(var2);
      }
    }
  }

  public static void unescapeJava(Writer out, String str) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    } else if (str != null) {
      int sz = str.length();
      StringBuilder unicode = new StringBuilder(4);
      boolean hadSlash = false;
      boolean inUnicode = false;

      for(int i = 0; i < sz; ++i) {
        char ch = str.charAt(i);
        if (inUnicode) {
          unicode.append(ch);
          if (unicode.length() == 4) {
            try {
              int value = Integer.parseInt(unicode.toString(), 16);
              out.write((char)value);
              unicode.setLength(0);
              inUnicode = false;
              hadSlash = false;
            } catch (NumberFormatException var9) {
              throw new RuntimeException("Unable to parse unicode value: " + unicode, var9);
            }
          }
        } else if (hadSlash) {
          hadSlash = false;
          switch (ch) {
            case '"':
              out.write(34);
              break;
            case '\'':
              out.write(39);
              break;
            case '\\':
              out.write(92);
              break;
            case 'b':
              out.write(8);
              break;
            case 'f':
              out.write(12);
              break;
            case 'n':
              out.write(10);
              break;
            case 'r':
              out.write(13);
              break;
            case 't':
              out.write(9);
              break;
            case 'u':
              inUnicode = true;
              break;
            default:
              out.write(ch);
          }
        } else if (ch == '\\') {
          hadSlash = true;
        } else {
          out.write(ch);
        }
      }

      if (hadSlash) {
        out.write(92);
      }

    }
  }

  public static String unescapeJavaScript(String str) {
    return unescapeJava(str);
  }
}
