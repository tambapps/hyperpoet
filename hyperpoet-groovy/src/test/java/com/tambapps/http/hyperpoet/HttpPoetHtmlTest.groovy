package com.tambapps.http.hyperpoet

import com.tambapps.http.contenttype.ContentType
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class HttpPoetHtmlTest extends JsonPlaceholderTest {

  private HttpPoet client = new HttpPoet(url: PLACEHOLDER_API_URL,
      contentType: ContentType.JSON, acceptContentType: ContentType.HTML).with {
    parsers[ContentType.HTML] = { ResponseBody body -> Jsoup.parse(body.string()) }
    it
  }

  @Test
  void testJsoup() {
    def html = client.get("/page.html")
    assertEquals(Document.class, html.class)
  }
}
