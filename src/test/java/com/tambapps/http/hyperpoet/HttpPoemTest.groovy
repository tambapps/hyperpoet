package com.tambapps.http.hyperpoet

import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull

class HttpPoemTest {
    private HttpPoet client = new HttpPoet(url: "https://jsonplaceholder.typicode.com",
            contentType: ContentType.JSON, acceptContentType: ContentType.JSON).tap {
        addInterceptor(new ConsolePrintingInterceptor())
    }


    @Test
    void testGet() {
        client.poem {
            def todo = get '/todos/1', headers(sisi: 'soso'), params(something: true)
            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)

            // query params can also work like that
            todo = get '/todos/1', [something: 'maybe']
            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)

            // and also like that
            todo = get path('/todos/1', something: false)
            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)
        }
    }

    @Test
    void testPatch() {
        client.poem {
            def todo = patch"/todos/1", body(title: 'new title')

            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)
        }
    }

    @Test
    void testPost() {
        client.poem {
            def todo = post"/todos", body(title: 'new title'), params(something: true)
            assertNotNull(todo.id)
            assertNotNull(todo.title)
            // yes, we can also do it like that
            todo = post"/todos", [title: 'new title']
            assertNotNull(todo.id)
            assertNotNull(todo.title)
        }
    }
}
