package com.tambapps.http.hyperpoet

import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull

class AsyncHttpPoetTest {

  private AsyncHttpPoet client = new AsyncHttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON)

  private final CountDownLatch countDownLatch = new CountDownLatch(1)

  @Test
  void testGet() {
    AtomicReference<Response> responseReference = new AtomicReference()
    AtomicReference reference = new AtomicReference()
    client.getAsync("/todos/1") { response, todo ->
      responseReference.set(response)
      reference.set(todo)
      countDownLatch.countDown()
    }

    countDownLatch.await()
    def todo = reference.get()
    println(todo)
    assertNotNull(todo.id)
    assertNotNull(todo.userId)
    assertNotNull(todo.title)
    assertNotNull(todo.completed)
    Response response = responseReference.get()
    assertEquals(200, response.code())
  }

  @Test
  void testGetNotFound() {
    AtomicReference<Response> responseReference = new AtomicReference()
    AtomicReference reference = new AtomicReference()
    client.getAsync("/todos/123456789") { response, todo ->
      responseReference.set(response)
      reference.set(todo)
      countDownLatch.countDown()
    }

    countDownLatch.await()
    def todo = reference.get()
    assertNull(todo)
    Response response = responseReference.get()
    assertEquals(404, response.code())
  }
}
