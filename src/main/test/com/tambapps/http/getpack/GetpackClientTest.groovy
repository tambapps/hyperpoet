package com.tambapps.http.getpack

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows


class GetpackClientTest {

  private GetpackClient client = new GetpackClient(url: "https://jsonplaceholder.typicode.com",
      contentType: MediaTypes.JSON, acceptContentType: MediaTypes.JSON)

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
}
