package com.tambapps.http.hyperpoet.invoke

import com.tambapps.http.hyperpoet.HttpMethod
import com.tambapps.http.hyperpoet.HttpPoet
import groovy.transform.CompileStatic
import lombok.SneakyThrows

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@CompileStatic
class OpenApiSpecPoeticInvokerTest {
  private static final OpenApiSpecPoeticInvoker invoker
  static {
    try {
      invoker = OpenApiSpecPoeticInvoker.fromSpec(
              OpenApiSpecPoeticInvokerTest.class.getResourceAsStream("/spec.yaml").text
      )
    } catch (IOException e) {
      throw new RuntimeException(e)
    }
  }
  private final HttpPoet poet = mock(HttpPoet.class)


  @Test
  void testNotFoundOperation() throws IOException {
    assertThrows(MissingMethodException, () -> invoke("getNothing"))
  }

  @Test
  void testGetPosts() throws IOException {
    invoke("getPosts")
    verify(poet, times(1)).method(Collections.emptyMap(), "/posts", HttpMethod.GET)
  }

  @Test
  void testGetPost() throws IOException {
    invoke("getPost", 1)
    verify(poet, times(1)).method(Collections.emptyMap(), "/posts/1", HttpMethod.GET)
  }


  @Test
  void testGetTodos() throws IOException {
    invoke("getTodos")
    verify(poet, times(1)).method(Collections.emptyMap(), "/todos", HttpMethod.GET)
  }

  @Test
  void testGetTodo() throws IOException {
    invoke("getTodo", 1)
    verify(poet, times(1)).method(Collections.emptyMap(), "/todos/1", HttpMethod.GET)
  }

  @SneakyThrows
  private void invoke(String operationId, Object... args) {
    invoker.invokeOrThrow(poet, operationId, args, new MissingMethodException(operationId, HttpPoet.class, args));
  }
}
