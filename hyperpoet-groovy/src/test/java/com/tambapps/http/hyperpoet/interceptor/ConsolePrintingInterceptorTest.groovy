package com.tambapps.http.hyperpoet.interceptor

import com.tambapps.http.hyperpoet.ContentType
import com.tambapps.http.hyperpoet.ErrorResponseHandlers
import com.tambapps.http.hyperpoet.HttpPoet
import com.tambapps.http.hyperpoet.JsonPlaceholderTest
import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor
import org.junit.jupiter.api.Test

class ConsolePrintingInterceptorTest extends JsonPlaceholderTest {

  private HttpPoet client = new HttpPoet(url: PLACEHOLDER_API_URL,
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).tap {
    addInterceptor(new ConsolePrintingInterceptor())
    errorResponseHandler = ErrorResponseHandlers.parseResponseHandler()
  }

  @Test
  void testGetAll() {
    client.get("/todos")
  }

  @Test
  void testGet() {
    client.get("/todos/1")
  }

  @Test
  void testGet_queryParameters() {
    client.get("/todos/1", params: [list: [1, 2, 3, 4], foo: 'tom&jerry', b: 2])
  }

  @Test
  void testGet_notFound() {
    client.get("/todos/123456789")
  }

  @Test
  void testGetWithQueryParam() {
    client.get("/todos/1", query: [a: 1, b: "2", c: "sisi"])
  }

  @Test
  void testPost() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1])
    println(p)
  }

  @Test
  void testPost_doNotPrint() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], print: false)
  }

  @Test
  void testPost_doNotPrintResponseBody() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printResponseBody: false)
  }

  @Test
  void testPost_doNotPrintRequestBody() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printRequestBody: false)
  }

  @Test
  void testPost_doNotPrintConsecutive() {
    println('print nothing')
    client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], print: false)
    println('do not print request body')
    client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printRequestBody: false)
    println('do not print response body')
    client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printResponseBody: false)
    println('print everything')
    client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1])
  }
}
