package com.tambapps.http.hyperpoet

import java.nio.charset.StandardCharsets

import static com.tambapps.http.hyperpoet.ContentType.HTML
import static com.tambapps.http.hyperpoet.ContentType.JSON
import static com.tambapps.http.hyperpoet.ContentType.WILDCARD
import static com.tambapps.http.hyperpoet.ContentType.XML
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

class ContentTypeTest {

  @Test
  void testSubtypes() {
    assertEquals(new ContentType("application","json-patch+json"), ContentType.valueOf('application/json-patch+json'))
  }
  @Test
  void testIncludes() {
    assertTrue(JSON.includes(new ContentType("application","problem+json")))
    assertTrue(JSON.includes(ContentType.valueOf('application/json-patch+json')))
    assertTrue(JSON.includes(JSON))
    assertTrue(new ContentType("application","*").includes(JSON))
  }
  @Test
  void testNotIncludes() {
    assertFalse(new ContentType("application","problem+json").includes(JSON))
    assertFalse(JSON.includes(new ContentType("application","*")))
    assertFalse(JSON.includes(XML))
  }

  @Test
  void testValueOf() {
    def type = ContentType.valueOf("application/problem+json;charset=UTF-8; lang=en")

    assertEquals("application", type.type)
    assertEquals("problem+json", type.subtype)
    assertEquals("json", type.subtypeSuffix.get())
    assertEquals([charset: 'UTF-8', lang: 'en'], type.parameters)
    assertEquals(StandardCharsets.UTF_8, type.charset.get())
  }

  @Test
  void testSort() {
    def types = [
        WILDCARD, JSON, new ContentType("application","problem+json"),
        XML, HTML, new ContentType("application","toto+xml"),
        new ContentType("application","*"),
        new ContentType("text","*")
    ]
    assertEquals([
        new ContentType("application","problem+json"),
        JSON,
        new ContentType("application","toto+xml"),
        XML,
        new ContentType("application","*"),
        HTML,
        new ContentType("text","*"),
        WILDCARD
    ], types.sort())
  }
}
