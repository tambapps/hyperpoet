package com.tambapps.http.hyperpoet

import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows

class HistoryTest {

  private HttpPoet client = new HttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).tap {
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
    enableHistory(10)
  }

  @BeforeEach
  void init() {
    client.enableHistory(10)
    client.history.clear()
  }

  @Test
  void testHistory() {
    def getTodo = client.get("/todos/1")
    def patchTodo = client.patch("/todos/1", body: [title: 'new title'])
    assertThrows(ErrorResponseException) {
      client.get("/todos/123456789") // the body is an empty json
    }
    def post = client.post('/posts', body: [title: 'foo', body: 'bar', userId: 1])

    assertEquals([getTodo, patchTodo, [:], post], client.history*.responseBody)
  }

  @Test
  void testHistoryLimit() {
    client.history.limit = 3
    def getTodo = client.get("/todos/1")
    def patchTodo = client.patch("/todos/1", body: [title: 'new title'])
    assertThrows(ErrorResponseException) {
      client.get("/todos/123456789") // the body is an empty json
    }
    def post = client.post('/posts', body: [title: 'foo', body: 'bar', userId: 1])
    def getPost = client.get("/posts/1")

    assertEquals([[:], post, getPost], client.history*.responseBody)
  }
}
