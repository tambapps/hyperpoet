package com.tambapps.http.hyperpoet

import groovy.json.JsonOutput
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

// TODO include surefire-plugin in order to make tests work
class ErrorResponseHandlersTest {
  private static final HttpPoet POET = new PrintingHttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).tap {
    errorResponseHandler = ErrorResponseHandlers.problemResponseHandler()
  }
  private static final PROBLEM = [type  : '/errors/type', title: 'Bad Request', status: 400,
                                  detail: 'You were bad', instance: '/instance/0', additionalProperty: 1]

  @BeforeAll
  static void init() {
    // simulate error
    POET.addInterceptor {
      return new Response.Builder()
              .code(400)
              .request(it.request())
              .protocol(Protocol.HTTP_1_0)
              .message("Bad Request")
              .body(ResponseBody.create(JsonOutput.toJson(PROBLEM).bytes, ContentType.JSON.toMediaType()))
              .build()
    }
  }

  @Test
  void testCatchProblem() {
    POET.errorResponseHandler = ErrorResponseHandlers.problemResponseHandler()
    def exception = assertThrows(ProblemResponseException.class) {
      POET.get("/something")
    }
    assertEquals(PROBLEM.type, exception.type)
    assertEquals(PROBLEM.title, exception.title)
    assertEquals(PROBLEM.detail, exception.detail)
    assertEquals(PROBLEM.instance, exception.instance)
    assertEquals(PROBLEM.status, exception.status)
    assertEquals(PROBLEM.additionalProperty, exception['additionalProperty'])
    assertEquals(PROBLEM.title, exception['title'])
    assertEquals(PROBLEM, exception.members)
  }

  @Test
  void testParseResponse() {
    POET.errorResponseHandler = ErrorResponseHandlers.parseResponseHandler(POET)

    def response = POET.post("/something", body: [a: "b"])

    assertEquals(PROBLEM.type, response.type)
    assertEquals(PROBLEM.title, response.title)
    assertEquals(PROBLEM.detail, response.detail)
    assertEquals(PROBLEM.instance, response.instance)
    assertEquals(PROBLEM.status, response.status)
  }
}
