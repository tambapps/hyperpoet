package com.tambapps.http.hyperpoet.io.poeticprinter;

/**
 * Interface used to pretty print request and response bodies
 */
public interface PoeticPrinter {

  /**
   * print the bytes data (usually a String) of a request/response body
   * @param bytes the body of the request/response
   */
  void printBytes(byte[] bytes);

}
