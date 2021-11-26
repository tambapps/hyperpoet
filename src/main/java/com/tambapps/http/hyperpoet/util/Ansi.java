package com.tambapps.http.hyperpoet.util;

public class Ansi {
  public static final String RESET = "\u001B[0m";
  public static final String BLUE_SKY = "\u001B[38;5;75m";
  public static final String RED = "\u001B[31m";
  public static final String BLUE = "\u001B[34m";
  public static final String GREEN = "\u001B[32m";

  public static void print(Object object) {
    System.out.print(object + RESET);
  }

  public static void println(Object object) {
    System.out.println(object + RESET);
  }

  public static void println() {
    System.out.print("\n" + RESET);
  }

  public static void print(String ansi, Object object) {
    System.out.print(ansi + object + RESET);
  }
}
