package com.tambapps.http.hyperpoet.invoke;

import com.tambapps.http.hyperpoet.AbstractHttpPoet;
import groovy.lang.MissingMethodException;

import java.io.IOException;

/**
 * Interface used to convert method call to a http poet request call
 */
public interface PoeticInvoker<T extends AbstractHttpPoet> {

  Object invokeOrThrow(T poet, String methodName, Object[] args, MissingMethodException e)
      throws IOException;

}
