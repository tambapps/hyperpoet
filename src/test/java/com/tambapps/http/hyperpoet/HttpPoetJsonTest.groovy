package com.tambapps.http.hyperpoet

import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows

class HttpPoetJsonTest {

  private HttpPoet client = new HttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).with {
    onPreExecute = { Request request, byte[] body ->
      println("headers\n${request.headers()}")
      if (request.method() in ['POST', 'PATCH', 'PUT']) {
        assertNotNull(body)
        println("Body: " + new String(body))
      }
    }
    onPostExecute = { Response response ->
      println(response.code())
    }
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
  void testGet_notFound() {
    def responseException = assertThrows(ErrorResponseException) {
      client.get("/todos/123456789")
    }
    assertEquals(404, responseException.response.code())
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

    // it seems that the API response has changed? these fields doesn't exist annymore
    //assertEquals('foo', post.title)
    //assertEquals('bar', post.body)
    //assertEquals(1, post.userId)
    assertEquals(101, post.id)
  }

  @Test
  void testGetOverrideParser() {
    def todo = client.get("/todos/1", parser: { ResponseBody body -> body.string() })
    assertEquals(String.class, todo.class)
  }
}
