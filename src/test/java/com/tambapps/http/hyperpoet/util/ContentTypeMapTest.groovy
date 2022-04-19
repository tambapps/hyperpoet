package com.tambapps.http.hyperpoet.util

import com.tambapps.http.hyperpoet.ContentType
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class ContentTypeMapTest {
  ContentTypeMap<String> map = new ContentTypeMap<>([
      (ContentType.JSON): "json",
      (new ContentType("something","*")): "something",
      (new ContentType("something","specific")): "specific",
  ])

  @Test
  void test() {
    assertEquals("json", map[ContentType.JSON])
    assertEquals("json", map[new ContentType("application","problem+json")])
    assertNull(map[new ContentType("application","jackson")])
    assertNull(map[new ContentType("application","problem+jackson")])

    assertEquals("something", map[new ContentType("something","special")])
    assertEquals("specific", map[new ContentType("something","specific")])

  }
}
