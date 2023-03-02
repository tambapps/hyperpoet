package com.tambapps.http.hyperpoet.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
}
