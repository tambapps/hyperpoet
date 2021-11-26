package com.tambapps.http.hyperpoet

import org.junit.jupiter.api.Test

class PrintingHttpPoetTest {

  private PrintingHttpPoet client = new PrintingHttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON)

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
  void testPost_doNotPrintResponseBody() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printResponseBody: false)
  }

  @Test
  void testPost_doNotPrintRequestBody() {
    def p = client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1], printRequestBody: false)
  }

}
