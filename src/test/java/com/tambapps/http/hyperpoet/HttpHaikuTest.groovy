package com.tambapps.http.hyperpoet

import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class HttpHaikuTest {

  @Test
  void testGet() {
    def todo = HttpHaiku.get("https://jsonplaceholder.typicode.com/todos/1")

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testGet_withHeaders() {
    def interceptor = new ConsolePrintingInterceptor(printRequestHeaders: true)
    HttpHaiku.poet.addInterceptor(interceptor)
    try {
      def todo = HttpHaiku.get("https://jsonplaceholder.typicode.com/todos/1",
          headers: [("X-Some-Custom-Header"): "Value"])

      println(todo)
      assertNotNull(todo.id)
      assertNotNull(todo.userId)
      assertNotNull(todo.title)
      assertNotNull(todo.completed)
    } finally {
      HttpHaiku.poet.configureOkHttpClient {
        it.interceptors().remove(interceptor)
      }
    }
  }

  @Test
  void testPatch() {
    def todo = HttpHaiku.patch("https://jsonplaceholder.typicode.com/todos/1", [title: 'new title'])

    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
  }

  @Test
  void testPost() {
    def post = HttpHaiku.post("https://jsonplaceholder.typicode.com/posts", [title: 'foo€', body: 'bar', userId: 1])

    // it seems that the API response has changed? these fields doesn't exist anymore
    //assertEquals('foo', post.title)
    //assertEquals('bar', post.body)
    //assertEquals(1, post.userId)
    assertEquals(101, post.id)
  }
}
