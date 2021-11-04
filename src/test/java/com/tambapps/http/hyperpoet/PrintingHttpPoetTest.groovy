package com.tambapps.http.hyperpoet

import org.junit.jupiter.api.Test

class PrintingHttpPoetTest {

  private PrintingHttpPoet client = new PrintingHttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON)

  @Test
  void testGet() {
    client.get("/todos/1")
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
    client.post("/posts", body: [title: 'foo', body: 'bar', userId: 1])
  }

}
