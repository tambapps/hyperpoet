package com.tambapps.http.hyperpoet.util

import org.junit.jupiter.api.Test

import java.time.LocalDate
import java.time.LocalDateTime

import static org.junit.jupiter.api.Assertions.assertEquals

class UrlBuilderTest {

  private static final String BASE_URL = 'https://example.com'

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
    assertEquals(url, builder.build())
  }

  @Test
  void testUrlWithQueryParams() {
    String url = "$BASE_URL?so=true&fi=false&a=123"
    UrlBuilder builder = new UrlBuilder(url)
    assertEquals(builder, new UrlBuilder().append(url))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParam('so', true).addParam('fi', false).addParam('a', 123))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParams(so: true, fi: false, a: 123))
    assertEquals(url, builder.toString())
    assertEquals(url, builder.build())
  }

  @Test
  void testUrlWithEncodedQueryParams() {
    String url = "$BASE_URL?array%5B4%5D=5&tom%26jerry=yes&pipe=%7C"
    UrlBuilder builder = new UrlBuilder(url)
    assertEquals(builder, new UrlBuilder().append(url))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParam('array[4]', 5).addParam('tom&jerry', 'yes').addParam('pipe', '|'))
    assertEquals(builder, new UrlBuilder(BASE_URL).addParams(['array[4]': 5, 'tom&jerry': 'yes', 'pipe': '|']))
    assertEquals(url, builder.build())
  }


  @Test
  void testLocalDateParam() {
    UrlBuilder builder = new UrlBuilder(BASE_URL)
    builder.addParam("date", LocalDate.of(2020, 01,01))
    assertEquals("$BASE_URL?date=2020-01-01".toString(), builder.build())
  }

  @Test
  void testLocalDateTimeParam() {
    UrlBuilder builder = new UrlBuilder(BASE_URL)
    builder.addParam("date", LocalDateTime.of(2020, 01,01, 01, 01))
    assertEquals("$BASE_URL?date=2020-01-01T01%3A01%3A00Z".toString(), builder.build())
  }

  @Test
  void testParamListRepeat() {
    UrlBuilder builder = new UrlBuilder(BASE_URL)
    builder.addParam('things', ['a', 'b', 'c'])
    assertEquals("$BASE_URL?things=a&things=b&things=c".toString(), builder.build())
  }


  @Test
  void testParamListComma() {
    UrlBuilder builder = new UrlBuilder(BASE_URL, UrlBuilder.QueryParamListComposingType.COMMA)
    builder.addParam('things', ['a', 'b', 'c'])
    assertEquals("$BASE_URL?things=a,b,c".toString(), builder.buildWithoutEncoding())
  }

  @Test
  void testParamListBrackets() {
    UrlBuilder builder = new UrlBuilder(BASE_URL, UrlBuilder.QueryParamListComposingType.BRACKETS)
    builder.addParam('things', ['a', 'b', 'c'])
    assertEquals("$BASE_URL?things=[a,b,c]".toString(), builder.buildWithoutEncoding())
  }
}
