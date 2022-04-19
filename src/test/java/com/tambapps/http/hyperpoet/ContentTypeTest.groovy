package com.tambapps.http.hyperpoet

import java.nio.charset.StandardCharsets

import static com.tambapps.http.hyperpoet.ContentType.JSON
import static com.tambapps.http.hyperpoet.ContentType.XML
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

class ContentTypeTest {

  @Test
  void testIncludes() {
    assertTrue(JSON.includes(new ContentType("application","problem+json")))
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
}
