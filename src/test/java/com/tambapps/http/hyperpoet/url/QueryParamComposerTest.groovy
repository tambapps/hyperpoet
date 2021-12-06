package com.tambapps.http.hyperpoet.url

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class QueryParamComposerTest {

    private final QueryParamComposer composer = new QueryParamComposer([:], MultivaluedQueryParamComposingType.REPEAT)
    @Test
    void testMap() {
        def map = [a:1, b:"2", c:"trois", d: [2, 2]]
        assertEquals([new QueryParam("a", "1"), new QueryParam("b", "2"), new QueryParam("c", "trois"), new QueryParam("d", "2"), new QueryParam("d", "2")].sort(), composer.compose(map).sort())
    }

    @Test
    void testObject() {
        def foo = new Foo()
        assertEquals([new QueryParam("a", "1"), new QueryParam("b", "2"), new QueryParam("c", "trois"), new QueryParam("d", "2"), new QueryParam("d", "2")].sort(), composer.compose(foo).sort())
    }

    @Test
    void testComposeToString() {
        def map = [email:"me+myself@i.com"]

        assertEquals("email=me%2Bmyself%40i.com", composer.composeToString(map))
    }
    class Foo {
        def a = 1
        def b = "2"
        def c = "trois"
        def d = [2, 2]
    }
}
