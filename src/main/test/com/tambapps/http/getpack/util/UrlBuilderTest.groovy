package com.tambapps.http.getpack.util

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class UrlBuilderTest {

  private static final String BASE_URL = 'http://example.com'

  @Test
  void testSimpleUrl() {
    assertEquals(BASE_URL, new UrlBuilder(BASE_URL).toString())
    assertEquals(BASE_URL, new UrlBuilder().append(BASE_URL).toString())
  }

  @Test
  void testUrlWithQueryParam() {
    String url = "$BASE_URL?so=true"
    UrlBuilder builder = new UrlBuilder(url)
    assertEquals(builder, new UrlBuilder().append(url))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParam('so', 'true'))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParams(so: true))
    assertEquals(url, builder.toString())
    assertEquals(url, builder.encoded())
  }

  @Test
  void testUrlWithQueryParams() {
    String url = "$BASE_URL?so=true&fi=false&a=123"
    UrlBuilder builder = new UrlBuilder(url)
    assertEquals(builder, new UrlBuilder().append(url))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParam('so', true).addParam('fi', false).addParam('a', 123))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParams(so: true, fi: false, a: 123))
    assertEquals(url, builder.toString())
    assertEquals(url, builder.encoded())
  }

  @Test
  void testUrlWithEncodedQueryParams() {
    String url = "$BASE_URL?array%5B4%5D=5&tom%26jerry=yes&pipe=%7C"
    UrlBuilder builder = new UrlBuilder(url)
    assertEquals(builder, new UrlBuilder().append(url))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParam('array[4]', 5).addParam('tom&jerry', 'yes').addParam('pipe', '|'))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParams(['array[4]': 5, 'tom&jerry': 'yes', 'pipe': '|']))
    assertEquals(url, builder.encoded())
  }
}
