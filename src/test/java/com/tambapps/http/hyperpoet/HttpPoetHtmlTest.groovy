package com.tambapps.http.hyperpoet

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class HttpPoetHtmlTest {

  private HttpPoet client = new HttpPoet(url: "https://fr.wikipedia.org",
      contentType: ContentType.JSON, acceptContentType: ContentType.HTML).with {
    parsers[ContentType.HTML] = { ResponseBody body -> Jsoup.parse(body.string()) }
    it
  }

  @Test
  void testJsoup() {
    def html = client.get("/wiki/Groovy_(langage)")
    assertEquals(Document.class, html.class)
  }
}
