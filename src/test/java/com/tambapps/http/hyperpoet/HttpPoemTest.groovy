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
            def todo = get '/todos/1'
            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)
        }
    }

    @Test
    void testPatch() {
        client.poem {
            def todo = patch"/todos/1", [title: 'new title']

            assertNotNull(todo.id)
            assertNotNull(todo.userId)
            assertNotNull(todo.title)
            assertNotNull(todo.completed)
        }
    }
}
