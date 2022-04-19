package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.ContentType.JSON;
import static com.tambapps.http.hyperpoet.ContentType.XML;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ContentTypeTest {

  @Test
  public void testIncludes() {
    assertTrue(JSON.includes(new ContentType("application","problem+json")));
    assertTrue(JSON.includes(JSON));
    assertTrue(new ContentType("application","*").includes(JSON));
  }
  @Test
  public void testNotIncludes() {
    assertFalse(new ContentType("application","problem+json").includes(JSON));
    assertFalse(JSON.includes(new ContentType("application","*")));
    assertFalse(JSON.includes(XML));
  }
}
