package com.tambapps.http.getpack

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull


class GetpackClientTest {

    @Test
    void testGet() {
        GetpackClient client = new GetpackClient("https://jsonplaceholder.typicode.com")

        def todo = client.get("/todos/1")

        println(todo)
        assertNotNull(todo.id)
        assertNotNull(todo.userId)
        assertNotNull(todo.title)
        assertNotNull(todo.completed)
    }

    @Test
    void testGetWithQueryParam() {
        GetpackClient client = new GetpackClient("https://jsonplaceholder.typicode.com")

        def todo = client.get("/todos/1", query: [a:1, b: "2", c: "sisi"])

        println(todo)
        assertNotNull(todo.id)
        assertNotNull(todo.userId)
        assertNotNull(todo.title)
        assertNotNull(todo.completed)
    }
}
