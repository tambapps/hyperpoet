package com.tambapps.http.hyperpoet

import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor
import com.tambapps.http.hyperpoet.invoke.OperationPoeticInvoker
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows

class HttpPoetJsonTest extends JsonPlaceholderTest {

  private HttpPoet client = new HttpPoet(url: PLACEHOLDER_API_URL,
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).with {
    addInterceptor(new ConsolePrintingInterceptor())
    poeticInvoker = new OperationPoeticInvoker(true)
    errorResponseHandler = ErrorResponseHandlers.throwResponseHandler()
    it
  }

  @Test
  void testGet() {
    def todo = client.get("/todos/1")

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testPatch() {
    def todo = client.patch("/todos/1", body: [title: 'new title'])

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testPatch_usingMethod() {
    def todo = client.modifyTodos(1, body: [title: 'new title'])

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testGet_usingMethod() {
    def todo = client.getTodos(1)

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testGet_notFound() {
    def responseException = assertThrows(ErrorResponseException) {
      client.get("/todos/123456789")
    }
    assertEquals(404, responseException.code)
  }

  @Test
  void testGetWithQueryParam() {
    def todo = client.get("/todos/1", query: [a: 1, b: "2", c: "sisi"])

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testPost() {
    def post = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1])

    assertEquals('foo', post.title)
    assertEquals('bar', post.body)
    assertEquals(1, post.userId)
    assertNotNull(post.id)
  }

  @Test
  void testPost_usingMethod() {
    def post = client.createPost(body: [title: 'foo', body: 'bar', userId: 1])

    assertEquals('foo', post.title)
    assertEquals('bar', post.body)
    assertEquals(1, post.userId)
    assertNotNull(post.id)
  }

  @Test
  void testGetOverrideParser() {
    def todo = client.get("/todos/1", parser: { ResponseBody body -> body.string() })
    assertEquals(String.class, todo.class)
  }
}
