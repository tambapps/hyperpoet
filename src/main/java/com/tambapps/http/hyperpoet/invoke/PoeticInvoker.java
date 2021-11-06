package com.tambapps.http.hyperpoet.invoke;

import com.tambapps.http.hyperpoet.HttpPoet;
import groovy.lang.MissingMethodException;

import java.io.IOException;

// TODO document about it
/**
 * Interface used to convert method call to an http poet request call
 */
public interface PoeticInvoker {

  Object invokeOrThrow(HttpPoet poet, String methodName, Object[] args, MissingMethodException e)
      throws IOException;

}
