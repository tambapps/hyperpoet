package com.tambapps.http.hyperpoet.util

import com.tambapps.http.hyperpoet.ContentType
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class ContentTypeMapTest {

  @Test
  void test1() {
    ContentTypeMap<String> map = new ContentTypeMap<>([
        (ContentType.JSON): "json",
        (new ContentType("something","*")): "something",
        (new ContentType("something","specific")): "specific",
    ])
    assertEquals("json", map[ContentType.JSON])
    assertEquals("json", map[new ContentType("application","problem+json")])
    assertNull(map[new ContentType("application","jackson")])
    assertNull(map[new ContentType("application","problem+jackson")])

    assertEquals("something", map[new ContentType("something","special")])
    assertEquals("specific", map[new ContentType("something","specific")])
  }

  @Test
  void test2() {
    ContentTypeMap<String> map = new ContentTypeMap<>([
        (ContentType.WILDCARD): "wildcard",
        (ContentType.JSON): "json",
        (new ContentType("something","*")): "something",
        (new ContentType("something","specific")): "specific",
    ])
    assertEquals("json", map[ContentType.JSON])
    assertEquals("json", map[new ContentType("application","problem+json")])
    assertEquals("wildcard", map[new ContentType("application","jackson")])
    assertEquals("wildcard", map[new ContentType("application","problem+jackson")])

    assertEquals("something", map[new ContentType("something","special")])
    assertEquals("specific", map[new ContentType("something","specific")])
  }

  @Test
  void testDefaultValue() {
    ContentTypeMap<String> map = new ContentTypeMap<>([
        (ContentType.JSON): "json",
    ])
    map.setDefaultValue("default")
    assertEquals("default", map[ContentType.XML])
    assertEquals("json", map[ContentType.JSON])
    assertEquals("json", map[new ContentType("application","problem+json")])

  }
}
