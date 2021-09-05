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
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).with {
    parsers[ContentType.HTML] = { ResponseBody body -> Jsoup.parse(body.string()) }
    it
  }

  private final CountDownLatch countDownLatch = new CountDownLatch(1)

  @Test
  void testGet() {
    AtomicReference reference = new AtomicReference()
    client.getAsync("/todos/1") { todo ->
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
  }

  @Test
  void testGetNotFound() {
    AtomicReference reference = new AtomicReference()
    client.getAsync("/todos/123456789") { todo ->
      reference.set(todo)
      countDownLatch.countDown()
    }

    countDownLatch.await()
    def todo = reference.get()
    assertNull(todo.id)
  }

  @Test
  void testGetNotFoundWithResponse() {
    AtomicReference<Response> reference = new AtomicReference()
    client.getAsync("/todos/123456789", includeResponse: true) { Response response ->
      reference.set(response)
      countDownLatch.countDown()
    }

    countDownLatch.await()
    Response response = reference.get()
    assertEquals(404, response.code())
  }
}
