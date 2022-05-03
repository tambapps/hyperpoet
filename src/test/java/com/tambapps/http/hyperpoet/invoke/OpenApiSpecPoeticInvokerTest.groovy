package com.tambapps.http.hyperpoet.invoke

import com.tambapps.http.hyperpoet.HttpPoet
import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor
import groovy.transform.CompileStatic
import lombok.SneakyThrows

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

@CompileStatic
class OpenApiSpecPoeticInvokerTest {
  private static final OpenApiSpecPoeticInvoker INVOKER = OpenApiSpecPoeticInvoker.fromSpec(
          OpenApiSpecPoeticInvokerTest.class.getResourceAsStream("/spec.yaml").text
  )
  private final HttpPoet poet = new HttpPoet("https://jsonplaceholder.typicode.com").tap {
    addInterceptor(new ConsolePrintingInterceptor())
  }

  @Test
  void testNotFoundOperation() throws IOException {
    assertThrows(MissingMethodException) {
      invoke("getNothing")
    }
  }

  @Test
  void testGetPosts() throws IOException {
    def posts = invoke("getPosts")
    assertTrue(posts instanceof List)
  }

  @Test
  void testGetPost() throws IOException {
    invoke("getPost", 1)
  }

  @Test
  void testGetPost_missingPathVariable() throws IOException {
    assertThrows(IllegalArgumentException) {
      invoke("getPost")
    }
  }


  @Test
  void testGetTodos() throws IOException {
    invoke("getTodos")
  }

  @Test
  void testGetTodo() throws IOException {
    invoke("getTodo", 1)
  }

  @Test
  void testListOperations() {
    assertEquals(["getPost", "getPosts", "getTodo", "getTodos"], INVOKER.listOperations())
  }

  @SneakyThrows
  private Object invoke(String operationId, Object... args) {
    return INVOKER.invokeOrThrow(poet, operationId, args, new MissingMethodException(operationId, HttpPoet.class, args));
  }
}
