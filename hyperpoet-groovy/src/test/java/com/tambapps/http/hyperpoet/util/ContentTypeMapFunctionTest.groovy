package com.tambapps.http.hyperpoet.util

import com.tambapps.http.hyperpoet.ContentType
import com.tambapps.http.hyperpoet.Function
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class ContentTypeMapFunctionTest {

  Function f0 = {}
  Function f1 = {}
  Function f2 = {}
  Function f3 = {}
  @Test
  void test1() {
    ContentTypeMapFunction map = new ContentTypeMapFunction([
        (ContentType.JSON): f1,
        (new ContentType("something","*")): f2,
        (new ContentType("something","specific")): f3,
    ])
    assertEquals(f1, map[ContentType.JSON])
    assertEquals(f1, map[new ContentType("application","problem+json")])
    assertNull(map[new ContentType("application","jackson")])
    assertNull(map[new ContentType("application","problem+jackson")])

    assertEquals(f2, map[new ContentType("something","special")])
    assertEquals(f3, map[new ContentType("something","specific")])
  }

  @Test
  void test2() {
    ContentTypeMapFunction map = new ContentTypeMapFunction([
        (ContentType.WILDCARD): f0,
        (ContentType.JSON): f1,
        (new ContentType("something","*")): f2,
        (new ContentType("something","specific")): f3,
    ])
    assertEquals(f1, map[ContentType.JSON])
    assertEquals(f1, map[new ContentType("application","problem+json")])
    assertEquals(f0, map[new ContentType("application","jackson")])
    assertEquals(f0, map[new ContentType("application","problem+jackson")])

    assertEquals(f2, map[new ContentType("something","special")])
    assertEquals(f3, map[new ContentType("something","specific")])
  }

  @Test
  void testDefaultValue() {
    ContentTypeMapFunction map = new ContentTypeMapFunction([
        (ContentType.JSON): f1,
    ])
    map.setDefaultValue(f0)
    assertEquals(f0, map[ContentType.XML])
    assertEquals(f1, map[ContentType.JSON])
    assertEquals(f1, map[new ContentType("application","problem+json")])

  }
}
